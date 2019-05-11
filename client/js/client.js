const { SendMsgRequest
      , ReceiveMsgsRequest
      , Msg
      , User
      } = require('./messaging_pb.js')
const { MessagingClient } = require('./messaging_grpc_web_pb.js')
const grpc = {}
grpc.web = require('grpc-web')

const client = new MessagingClient('http://'+window.location.hostname+':8081', null, null)

// random string
var myname = (new URLSearchParams(window.location.search)).get('name')
if (myname === null) {
    myname = "no_name_man"
}
console.log("my name is", myname)

// Elm -------------------------------------------------------------------------

var app = Elm.Main.init(
    {flags: {myname: myname}}
)
app.ports.sendMsg.subscribe(function(arg){
    // gRPC sendMsg
    const toName = arg[0]
    const content = arg[1]
    console.log(toName, content)

    const request = new SendMsgRequest()
    const msg = new Msg()
    const user = new User()
    const to = new User()

    user.setName(myname)
    to.setName(toName)
    msg.setFrom(user)
    msg.setTo(to)
    msg.setContent(content)
    request.setUser(user)
    request.setMsg(msg)

    client.sendMsg(request, {}, (err, ret) => {
        if (err || ret === null) {
            throw err
        }
        console.log(ret)
    })
})

// gRPC receiveMsgs
const request2 = new ReceiveMsgsRequest()
const user = new User()
user.setName(myname)
request2.setUser(user)

const stream = client.receiveMsgs(request2, null)
stream.on('data', function(response) {
    console.log(response.getMsg())
    const msg = response.getMsg()
    const from = msg.getFrom().getName()
    const to = msg.getTo().getName()
    const content = msg.getContent()
    app.ports.receiveMsgs.send(
        { from: from
        , to: to
        , content: content
        }
    )
})
stream.on('status', function(status) {
    console.log(status)
})
stream.on('end', function(end) {
    // stream end signal
    console.log(end)
})
