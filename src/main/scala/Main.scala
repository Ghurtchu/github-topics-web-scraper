import zio.{Exit, *}
import zio.console.*

import java.io.IOException

object Main extends zio.App {

  given baseUrl: URL = URL("https://github.com")

  private val optionMapping: Map[String, String] = Map(
    "1" -> "Download, parse and store top 30 most popular projects by topics as a CSV file.",
    "2" -> "Exit."
  )

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode


  val program: ZIO[Console, Throwable, Unit] = for {
    _          <- putStrLn("Welcome to Github Scraper") *> putStrLn("Choose one of the following options")
    _          <- displayOptions()
    userOption <- chooseOption()
    _          <- processAndThenExecute(userOption)
  } yield ()

  private def displayOptions(): ZIO[Console, IOException, Unit] = ZIO.succeed {
    optionMapping.foreach((k, v) => println(k concat " = " concat v))
  }

  private def chooseOption(): ZIO[Console, IOException, String] = for {
    choice <- getStrLn
    _      <- putStrLn(s"Executing command: ${optionMapping.getOrElse(choice, "Exit")}")
  } yield choice

  private def processAndThenExecute(userOption: String)(using url: URL): ZIO[Console, Throwable, Unit] =
    userOption match
      case "1" => Scraper.scrape(url.value)
      case _   => ZIO.succeed(())


  case class URL(value: String)

}


