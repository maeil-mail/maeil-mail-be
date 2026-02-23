## flow

job name : mailSendJob

스텝 구성 :
- mailGenerateStep
- mailSendStep
- changeSequenceStep

리스너 : 
- mailSendJobReportListener
  - afterJob 행위로 메일 전송 결과 리포트를 관리자에게 전송한다.

**mailGenerateStep** : subscribe의 유형에 맞춰서 메일 메시지를 생성하고, forward 데이터를 저장한닥.
- 이 과정에서 중복 발송은 없고, 청크 트랜잭션 단위로 관리되므로 실패 지점부터 정확히 재실행할 수 있다.
- reader : `JpaCursorItemReader` 
- processor : `CompositeItemProcessor<Subscribe, MailMessage> mailSendProcessor`
  - CompositeItemProcessor는 여러 위임 대상 ItemProcessor를 순차적으로 실행한다.
  - 실행 프로세서는 filterSubscribeProcessor, mailMessageProcessor
  - 이때 mailMessageProcessor는 ClassifierCompositeProcessor를 사용한다.
    - 이는 Spring Classifier를 사용해서 아이템을 처리할 ItemProcessor로 라우팅함
    - MailSendProcessorClassifier를 사용해서 subscribe의 frequency를 조회하여 daily, weekly 중 하나를 선택함
  - 요약 : 두가지 processor를 순차적으로 실행(filter, mailMessageProcessor(분류해서 조건부 실행))한다. 
- writer : `ClassifierCompositeItemWriter<MailMessage> mailSendWriter` -> forwardLog 저장
    - processor와 동일
- 해당 스텝에서 고려 지점은 다음과 같음
  - `청크 사이즈`
  - `subscribe_question 데이터 관련 네트워크 io 최적화`
  - `reader 선택 관련 근거`

**mailSendStep** : 메일을 전송한다.

- reader : `JpaCursorItemReader`
- processor : `ForwardProcessor`
  - forwordLog가 processing인 경우, 필터링한다.
- writer : `ForwardWriter`
  - statusBatchChanger.chageState는 트랜잭션 전파 옵션이 Propagation.REQUIRES_NEW이다.
  - 청크 아이템 단위로 새로운 트랜잭션을 열어서 아이템의 상태를 processing으로 변경한다. (호출 불확정 상태 생략이후 재시도)
  - forwardSender를 사용해서 동기 방식으로 전달하는데, 비동기인 경우, 예외 전파가 안돼서 동기로 바꿨음.
  - 이 부분은 promise.all 어쩌구처럼 전체 청크를 동시에 시작한 이후, 하나라도 에러가 발생하면 청크 트랜잭션 자체를 롤백 시키는 방향을 사용하면 될듯
- 해당 스텝에서 고려 지점은 다음과 같음
  - `reader 선택 근거`
  - `청크 사이즈`
  - `sendMailSync 개선`
  - `processing 변환 트랜잭션이 청크 트랜잭션과 분리되는지 테스팅`
- 중요 : processing에 대한 재처리는 추가적인 인프라 비용이 필요하기 때문에 생략하는 것으로 결정

**changeSequenceStep** :
- subscribeRepository.increaseNextQuestionSequence를 여전히 사용하는데, 이 쿼리 오래걸리는데 개선해야함

## memo

- 일간 메일 발송
    - (reader or processor): 일간 구독자를 읽어온다. -> Jpa or Jdbc Paging Item Reader
        - 유효 데이터(아래 조건을 모두 충족해야 한다.) :
            - deleted_at이 7시 이후
            - null인 구독자 레코드
            - DAILY 전송 주기를 가진 사람
            - created_at이 발송일 7시 이전인 경우
        - 엣지 케이스 : 오전 7시 1분에 발송 주기를 변경하는 사용자는 어떻게 처리할 것인가?
            - 기존 : 오전 7시 데이터를 스냅샷을 찍어서 사용한다.
            - 대안 : 
              - cursor을 사용하는 방식
              - updated_at을 기준으로 수정? 
                - ex) processor에서 updated_at이 오전 7시 이후인 경우, 전송 주기 체크하여 처리 
                - 위 방식의 경우, DAILY라면 보낼지 보내지 않을지?를 결정해야 한다.
                  - 만약, 보내지 않는다를 가정하면 7시 이후에 수정하는 것을 시스템이 용인하는 것인가? 그러면 발송 과정 중에 구독하는 경우도 전달 해야하지 않는가?
    - (processor or writer) 일간 구독자에 대한 메일을 생성한다.
        - I chunk가 subscribe면, O chunk가 mailMessage로 만듦? 필요한 데이터는 프로세서에서 DB 콜 해야하나? -> 청크만큼 반복 조회?
        - choice question policy 사용하면 됨
        - 고민 필요한 부분 : 로컬 캐시 사용 여부
    - (writer - 청크 단위 처리가 효율적) 받은 질문지 내역을 추가한다.
        - 과거에 받았던 질문지 내역을 제거하고, 새로운 질문지 내역을 추가한다.
        - 고민 필요한 부분 : n + 1 및 삭제 처리 시 효율성 고려
    - (흠.. 이건 뭘까) 메일을 발송한다.
    - (tasklet) 일간 구독자의 시퀀스를 1씩 증가시킨다.
    - (tasklet) 관리자에게 발송 내역을 전송한다.

## Forwad 로그가 필요한 근거

- 그 관점에서 subscribe_question을 사용해서 호출 불확정을 식별하려면 pending, processing과 같은 상태가 필요하다.
  - send가 호출되고 fail, success같은 경우에는 실제로 메일이 발송을 시도한 케이스다. 따라서, 이는 호출 불확정 상태가 아닐것이다.
  - pending이 있어야하는 이유는 subscribe_question이 메일을 발송한 뒤에 생성되기 떄문이다.
    - 만약에 특정 청크를 재시도한다고 했을 때, 해당 item에 대한 subscribe_question이 존재하지 않을 것이다. -> 청크 트랜잭션이 롤백되거나 아니면 개별 트랜잭션이 실패했을 때.
    - 그럼 재시도를 할때 시도는 한건지 아니면 뭐 호출 불확정인지 알 수가 없음~ 
  
## 내결함

### 문제 정의

- AbstractMailSender가 mail을 전송하고 성공 실패 여부에 따라서 각 sender 구현체에 정의된 방식으로 subscribe_question을 저장한다.
- reader, processor, writer에서 청크를 재소비할 수 있다. 예를 들어, 배치 프로세스가 의도치않게 종료된 경우 reader에서는 읽은 지점부터 다시 읽기도 하며, 내결함 기능을 쓸때는 재시도 정책에 따라서 청크를 다시 읽을 수 있다.
- 따라서, 호출 불확정 상태가 발생할 수 있다. (기존에도 이 문제가 있었다.)

### 아이디어 1

- 처음에는 아래처럼 AbstractMailSender에 동기 + 트랜잭션 분리 방식의 메서드를 하나 추가했다. 
```
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendMailSync(T message) {
        sendMail(message);
    }
```
- 이 방식을 쓰면 writer에서 item 별로 개별 트랜잭션을 적용하므로 각 sendMail 작업은 청크 트랜잭션에 영향을 안준다고 생각했다.
- processor는 멱등하게 구현이 되어 있긴 하다.
- 이후에는 별도의 대사 스텝을 추가해서 전송 건수가 실발송 건수와 다르다면 대사 스텝을 실패처리하고 ->  recovery 스텝을 하나 추가할 생각이었다.
  - 스프링 배치의 내결함 기능을 안쓰는 방식
- recovery 스텝이나 대사 스텝을 생각하기 이전에 사실 이 방식은 문제가 있다고 판단했다.
  - 만약에 이 스프링 배치 프로세스가 모종의 이유로 다운된다면? -> 어디서부터 재시도를 할건가? -> 멱등과 관련된 복잡한 문제가 발생한다. 뿐만 아니라 item별 db, netwrok io가 발생한다는 점에서 성능의 저하가 있다.

### 아이디어 2 - 호출 불확정 식별을 위함

- 만약 mailsendstep이 실패를 했다면.. 청크가 재소비될 수 있지?
- 그럼 writer에서 subscribe_question이 없는 애들만 실행하면 되지 않을까?라는 생각을 했음
  - 하지만, 이게 녹록치가 않음.. item이 100개라고 가정했을때 58번째를 전송하고 해당 subscribe_question을 저장하기 이전에 배치 프로세스가 중단됐다.
  - 그러면 57번째까지는 시도 1 방식에 근거하면 데이터가 있을거다.
  - 하지만 58번째가 데이터가 없음 -> 이 친구는 호출 불확정이니까 무시를 해
  - 그러면, 58번째는 그냥 무시하고 59번째 할려고하는데.. 얘도 없어.. 얘는 호출 불확정이 아닌데. 호출 불확정을 식별하는 기준이 subscribe_question이 없기 때문이야.
    - 음.. 그러면 이전 item이 실패했다... 이전 item이 subscribe_question이 없는 첫번째 데이터니까 얘만 무시하기 위해서 뭔가...step execution context를 이용할 수 있지 않을까?
  - (한계) 그러면.. 첫 시도에는 어떻게해? 얘는 항상 모든 item이 subscribe_question이 없을텐데.. 첫번째 item은 발송이 안될거 같은데?

### 아이디어 3 - 호출 불확정 식별과 db io 횟수를 줄이기 위함 - 상태 추가

- db io를 최대한 횟수를 줄이면서, 호출 불확정을 식별하기 위해서 '상태'를 추가하자
- subscribe_question에 NEW 상태를 추가하자.
- 메일 발송 writer를 다음과 같은 책임만 가지도록 한다.
  - 메일 발송을 위한 subscribe_question 생성
  - 이렇게 하면 subscribe_question은 청크 트랜잭션을 쓸 수 있어서 multi value insert 라던가 db 왕복 횟수를 줄일 수 있을 것이라는 가설 -> outbox 느낌이야.
- 추가로 메일 send step을 별도로 둔다.
  - 여기서 subscribe_question의 new인 애들을 reading하고
  - 이 친구들을 전송한다.
- 다만, 이 경우에도 똑같이 아이디어 2번의 문제가 발생한다. 
  - subscribe_question의 특정 청크를 재시도하는데, 전부다 NEW 잖아.. 알 수가 없음.. 결국 아이디어 1처럼 item 별 트랜잭션을 열어서 성공 실패를 변경해줘야하는데.. ;;
  - 57번까지는 실패 성공 처리인데.. 58번이 new면 안돼... 1번 아이템은 항상 new 잖아...
  - 뿐만아니라 subscribe_question을 outbox 느낌의 레코드로 추가하면..
  - 이거는 다른 곳에서도 쓰이는 엔티티이며. 추가로 주간 전송인 경우에는 5개 생겨.. 100개 아이템의 청크가 99번째에 주간 발송 subscribe_question이면.. 어쩌려고?
-> 결국 db io는 오히려 추가될거고... subscribe_question이 2가지 책임이 있어서 변경에 취약하고 확장이 어려운 데이터 설계라고 생각함. 그리고... new 상태로만은 호출 불확정을 알 수 없음

### 아이디어 4 - pending, processing 추가

- 별도의 forwardlog 테이블을 만들고 전송 데이터를 저장한다. 기존 mailSendwriter에서는 subscribe_question을 저장하고, forwardlog도 추가한다.
- 최초에는 pending으로 저장해준다.
```
--- 청크 트랜잭션 (pending log 만들기) ---
items.forEach(this::saveSubscribeQuestion);
items.forEach(this::savePendingForwardLog);
```

```
--- 별도 트랜잭션 (pending log 일괄 업데이트)
forwardlogs.forEach(it -> it.setStatus(PROCESSING) // 처리 중 상태로 마킹

--- 청크 트랜잭션 (메일 발송 시도) ---
messages = forwardlogs.map(this::mapToMessage) // 메시지로 변경
mailSender.send(messages); // 발송
    - mailSender 내부에서 log 업데이트를 수행
```

- 재시도할때는 pending이나 fail만 재시도하면 된다!
- PROCESSING인 경우에는 호출 불확정이라서 다시 보내지 않게 구현할 수 있을거 같다.
- 다만.. 호출 불확정인 데이터가 청크 단위 연대 책임이다.
  - 이건 트레이드오프해야하는데.. 개별 트랜잭션을 열어서 마킹 처리를 하고 save 시도를 하게 된다면.. io 횟수가 늘어날 것이다.
