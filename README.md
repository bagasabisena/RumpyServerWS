# Rumpy Server
Rumpy is a chat service that I developed back in 2012 for hobby project when I am still learning to program at the first time. The server is part of a stack consist of:

- Server.
- [Client](https://github.com/bagasabisena/RumpyClient) app for Android.
- JSON-based [protocol](https://www.github.com/bagasabisena/Stanza).

The server is built in Java on top of [WebSocket](http://en.wikipedia.org/wiki/WebSocket) using excellent networking library [Netty Framework](http://www.netty.io). Once the client is connected, the client and server will try to make the connection open by sending periodic websocket ping.
