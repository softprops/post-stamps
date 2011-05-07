package telegram

object Post {
   import java.util.Properties
   import javax.activation._
   import javax.mail._
   import javax.mail.internet._

  implicit def e2e(e1: Envelop => Envelop) = new {
    def |(e2: Envelop => Envelop) = e1 andThen e2
  }

  implicit def s2b(str: String) = Txt(str)

  def smtps[T](host: String, user: String, pass: String)(f: Transport => T)(implicit s: Session): Either[Throwable, T] = {
    val t = s.getTransport("smtps")
    try {
      t.connect(host, user, pass)
      Right(f(t))
    }
    catch { case e => Left(e) }
    finally { t.close }
  }

  def session(props: Seq[(String, String)]) =
    Session.getInstance((new Properties() /: props)(
      (a,e) => { a.put(e._1, e._2); a }
    ))

  trait Body extends (Message => Message)

  object Body {
    val empty = new Body {
       def apply(m: Message) = {
         m.setText("")
         m
       }
    }
  }
  case class Txt(s: String) extends Body {
    def apply(m: Message) = {
      m.setText(s)
      m
    }
  }

  object Envelop {
    val empty = Envelop(None, None, None, None, None)
  }
  case class Envelop(from: Option[String], to: Option[Seq[String]],
                     subject: Option[String], body: Option[Body], headers: Option[Seq[(String,String)]]) {
    def targets = Map("from" -> from, "to" -> to)
  }

  object Pack {
    def apply(x: (Envelop, Session)): Either[String, Message] = x match {
      case (e, s) => e match {
        case Envelop(Some(from), Some(to), sub, body, headers) =>
          val msg = new MimeMessage(s)
          msg.setFrom(new InternetAddress(from))
          msg.setRecipients(Message.RecipientType.TO, to.map(new InternetAddress(_).asInstanceOf[javax.mail.Address]).toArray)
          sub.map(msg.setSubject(_))
          headers.map(_.foreach(_ match { case (k,v) => msg.setHeader(k, v) }))
          body match {
            case Some(b) => b(msg)
            case _ => Body.empty(msg)
          }
          Right(msg)
        case invalid => Left("Missing %s" format(((Nil:List[String]) /: invalid.targets)(
          (a,e) => e match { case (k, None) =>  k :: a case _ => a }).mkString(" and ")
        ))
      }
    }
  }

  def to(addrs: Seq[String]) = (_:Envelop) match {
    case Envelop(from, _, sub, body, headers) => Envelop(from, Some(addrs), sub, body, headers)
  }

  def to(addr: String) = (_:Envelop) match {
    case Envelop(from, _, sub, body, headers) => Envelop(from, Some(Seq(addr)), sub, body, headers)
  }

  def from(addr: String) = (_:Envelop) match {
    case Envelop(_, to, sub, body, headers) => Envelop(Some(addr), to, sub, body, headers)
  }

  def subject(sub: String) = (_:Envelop) match {
    case Envelop(from, to, _, body, headers) => Envelop(from, to, Some(sub), body, headers)
  }

  def body(body: Body) = (_:Envelop) match {
    case Envelop(from, to, sub, _, headers) => Envelop(from, to, sub, Some(body), headers)
  }

  def headers(headers: (String, String)*) = (_:Envelop) match {
    case Envelop(from, to, sub, body, _) => Envelop(from, to, sub, body, Some(headers))
  }

  /** adds implicit convertion for `viaSmtps `*/
  implicit def e2t(e: Either[String, Message]) = new {
    def viaSmtps(host: String, user: String, pass: String)(implicit s: Session) = {
      e.fold({ err =>
        println(err)
      }, { msg =>
        smtps(host, user, pass) { transport =>
          println("sending msg %s" format msg)
          transport.sendMessage(msg, msg.getAllRecipients)
        }
      })
    }
  }

  def apply[T](f: => Envelop => Envelop)(implicit s: Session) = Pack(f(Envelop.empty), s)

}
