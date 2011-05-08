# telegram

![teletype](http://upload.wikimedia.org/wikipedia/commons/8/89/WACsOperateTeletype.jpg)
The missing mailer for scala

## usage

note: this interface is subject to __change__

    import telegram.Post
    import Post._

    val (host, user, pass) = ("smtp.gmail.com", "you@gmail.com", "y0U@gM@1L")
    implicit val ses = session(("mail.smtps.auth","true"))

    // blocking
    Post(
      from("doug@domain.com")
      | to("doug.telegram@gmail.com")
      | subject("hi")
      | body("well hello there")
      ) viaSmtps(host, user, pass)

    // non blocking
    Post(
      from("doug@domain.com")
      | to("doug.telegram@gmail.com")
      | subject("hi")
      | body("well hello there, thanks for not blocking")
      ) viaSmtps_!(host, user, pass)

### Defining your own handlers

`Post(...)` returns an `Either[String, Message]` value. To add more handlers you can either pass the value to your own function
or create an implicit conversion on `Either[String, Message]` which should provide more dsl-like client code

## todo

* multi part msgs
* datasource/file interface for attaching files
* query interface

Doug Tangren (softprops) 2010
