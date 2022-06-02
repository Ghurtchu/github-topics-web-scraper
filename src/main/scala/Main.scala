import zio.{Exit, *}
import zio.console.*

import java.io.IOException

object Main extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = program.exitCode

  val program: ZIO[Console, Throwable, Unit] = for {
    _                <- putStrLn("Welcome to ScalaJobs.com Scraper")
    _                <- putStrLn("Choose one of the following options")
    _                <- displayOptions()
    userOption       <- chooseOption()
    _                <- Scraper.scrape()
  } yield ()

  private def displayOptions(): ZIO[Console, IOException, Unit] = {
    putStrLn("Options: ") *>
    putStrLn("1 - Download all data") *>
    putStrLn("2 - Exit")
  }

  private def chooseOption(): ZIO[Console, IOException, String] = for {
    choice <- getStrLn
    _      <- putStrLn(s"Executing command: $choice")
  } yield choice


}


