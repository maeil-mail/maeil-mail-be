/**
 *  - 처리율 제한 : https://github.com/animir/node-rate-limiter-flexible
 *  - smtp 서버 구성 : https://nodemailer.com/extras/smtp-server
 *  - nodemailer sendMail 커스텀 소스 : https://nodemailer.com/message/custom-source
 *     - proxy에서 mailhog로 메일 메시지를 전달만 하기 때문에, 이미 포맷된 message가 존재한다. (커스텀 소스 문서 참고)
 */

const {SMTPServer} = require('smtp-server');
const nodemailer = require('nodemailer');
const {RateLimiterMemory} = require('rate-limiter-flexible');
const {buffer} = require('node:stream/consumers');

const TPS = Number(process.env.TPS);
const FORWARD_HOST = process.env.FORWARD_HOST;
const FORWARD_PORT = Number(process.env.FORWARD_PORT)

const transport = nodemailer.createTransport({
    host: FORWARD_HOST,
    port: FORWARD_PORT,
    secure: false
});

const limiter = new RateLimiterMemory({points: TPS, duration: 1});

const server = new SMTPServer({
    disabledCommands: ['AUTH', 'STARTTLS'],
    hideSTARTTLS: true,
    size: 25 * 1024 * 1024,

    onMailFrom(address, session, callback) {
        limiter.consume('global', 1)
            .then(() => callback())
            .catch(() => {
                const err = new Error('4.4.5 Throttling failure: Maximum sending rate exceeded');
                err.responseCode = 454;
                callback(err);
            });
    },

    async onData(stream, session, callback) {
        try {
            const raw = await buffer(stream);
            await transport.sendMail({
                envelope: {
                    from: session.envelope.mailFrom.address,
                    to: session.envelope.rcptTo.map(r => r.address),
                },
                raw,
            });
            callback();
        } catch (e) {
            const err = new Error('4.3.0 Temporary local problem');
            err.responseCode = 451;
            callback(err);
        }
    },
});

server.listen(587, () => console.log(`SMTP throttle proxy :587 -> ${FORWARD_HOST}:${FORWARD_PORT} (tps=${TPS})`));
