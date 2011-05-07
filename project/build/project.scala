import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  val mail = "javax.mail" % "mail" % "1.4"
}
