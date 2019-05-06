const { SendMsgRequest
      , ReceiveMsgsRequest
      , Msg
      , User
      } = require('./messaging_pb.js')
const { MessagingClient } = require('./messaging_grpc_web_pb.js')
const grpc = {}
grpc.web = require('grpc-web')

// Elm -------------------------------------------------------------------------
var app = Elm.Main.init({})
app.ports.sendMsg.subscribe(function(arg){
    const from = arg[0]
    const to = arg[1]
    const content = arg[2]
    console.log(from, to, content)
})
// -----------------------------------------------------------------------------

const request = new SendMsgRequest()
const msg = new Msg()
const user = new User()
const to = new User()
user.setName("hoge")
to.setName("fuga")
msg.setFrom(user)
msg.setTo(to)
msg.setContent("aaaaaaaaaaaaaa_aaaaaa")
request.setUser(user)
request.setMsg(msg)

const client = new MessagingClient('http://'+window.location.hostname+':8081', null, null)
// client.sendMsg(request, {}, (err, ret) => {
//     if (err || ret === null) {
//         throw err
//     }
//     console.log(ret)
// })

const request2 = new ReceiveMsgsRequest()
request2.setUser(user)

const stream = client.receiveMsgs(request2, null)
stream.on('data', function(response) {
    console.log(response.getMessage())
})
stream.on('status', function(status) {
    console.log(status.code)
    console.log(status.details)
    console.log(status.metadata)
})
stream.on('end', function(end) {
    // stream end signal
})
