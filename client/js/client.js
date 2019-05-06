const { SendMsgRequest
      , ReceiveMsgsRequest
      , Msg
      , User
      } = require('./messaging_pb.js');
const { MessagingClient } = require('./messaging_grpc_web_pb.js');
const grpc = {};
grpc.web = require('grpc-web');

const request = new SendMsgRequest();
const msg = new Msg();
const user = new User();
const to = new User();
user.setName("hoge");
to.setName("fuga");
msg.setFrom(user);
msg.setTo(to);
msg.setContent("aaaaaaaaaaaaaa_aaaaaa");
request.setUser(user);
request.setMsg(msg);

const client = new MessagingClient('http://'+window.location.hostname+':8081', null, null);
client.sendMsg(request, {}, (err, ret) => {
    if (err || ret === null) {
        throw err;
    }
    console.log(ret)
});
