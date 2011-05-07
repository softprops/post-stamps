# telegram

![teletype](http://en.wikipedia.org/wiki/File:WACsOperateTeletype.jpg)
The missing mailer for scala

## usage

    import telegram.Post
    import Post._

    implicit val session = session(("key","val"))

    Post(from("doug@domain.com")
      | to("doug@domain2.com")
      | subject("hi")
      | body("well hello there")
      ) viaSmtps(host, user, pass)

## todo

* multi part msgs
* datasource/file interface
* query interface

Doug Tangren (softprops) 2010
