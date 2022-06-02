import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.NodeFilter
import zio.*
import zio.console.*
import scala.jdk.CollectionConverters._


import java.util.stream.Collectors

object Scraper {

  val url: String = "https://github.com/topics/"
  val paragraphTag = "p"
  val f3ClassName = "f3 lh-condensed mb-0 mt-1 Link--primary"
  val linkTag = "a"
  val linkClassName = "no-underline flex-1 d-flex flex-column"
  val href = "href"

  def scrape(): ZIO[Console, Throwable, Unit] = for {
    doc          <- ZIO(Jsoup.connect(url).get())
    body         <- ZIO(doc.body())
    paragraphs   <- ZIO[List[Element]](body.select(paragraphTag).stream().filter(_.hasClass(f3ClassName)).collect(Collectors.toList).asScala.toList)
    titles       <- ZIO(paragraphs.map(_.text()))
    hyperlinks   <- ZIO[List[Element]](body.select(linkTag).stream().filter(_.hasClass(linkClassName)).collect(Collectors.toList).asScala.toList)
    descriptions <- ZIO(hyperlinks.map(_.text()))
    urls         <- ZIO(hyperlinks.map(elem => url concat elem.attr(href)))

  } yield ()

}
