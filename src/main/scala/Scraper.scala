import com.github.tototoshi.csv.*
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.{Elements, NodeFilter}
import zio.*
import zio.console.*

import scala.jdk.CollectionConverters.*
import java.util.stream.Collectors
import scala.concurrent.Future

object Scraper {

  val baseUrl = "https://github.com"
  val topicsUrl: String = baseUrl concat "/topics/"
  val paragraphTag = "p"
  val href = "href"
  val descriptionClassName = "f5 color-fg-muted mb-0 mt-1"
  val paragraphTitleClassName = "f3 lh-condensed mb-0 mt-1 Link--primary"

  def filterElements(paragraphs: List[Element])(f: Element => Boolean): Task[List[Element]] = Task.effect(paragraphs.filter(f))

  def mapParagraphs[A](paragraphs: List[Element])(f: Element => A): Task[List[A]] = Task.effect(paragraphs.map(f))

  def fetchDocByURL(url: String): Task[Document] = Task.effect(Jsoup.connect(url).get())

  def extractBody(doc: Document): Task[Element] = Task.effect(doc.body())

  def selectAllParagraphs(body: Element): Task[List[Element]] = Task.effect(body.select(paragraphTag).stream().collect(Collectors.toList).asScala.toList)

  def extractUrls(hyperlinks: List[Element])(url: String): Task[List[String]] = Task.effect {
    val href = "href"

    hyperlinks.map(elem => url concat elem.attr(href))
  }

  def combine(titles: List[String], descriptions: List[String], urls: List[String]): UIO[List[Seq[String]]] = ZIO.succeed {
    titles.zip(descriptions.zip(urls)).map { tuple =>
      Seq(tuple._1, tuple._2._1, tuple._2._2)
    }
  }

  def scrape(): ZIO[Console, Throwable, Unit] = for {
    doc             <- fetchDocByURL(topicsUrl)
    body            <- extractBody(doc)
    allParagraphs   <- selectAllParagraphs(body)
    titleParagraphs <- filterElements(allParagraphs)(_.hasClass(paragraphTitleClassName))
    titles          <- parseToTitles(titleParagraphs)
    hyperlinks      <- parseToHyperLinks(body)
    descriptions    <- filterElements(allParagraphs)(_.hasClass(descriptionClassName)).flatMap(mapParagraphs(_)(_.text()))
    urls            <- extractUrls(hyperlinks)(baseUrl)
    topicDocuments  <- ZIO.foreachPar(urls)(url => ZIO.effect(Jsoup.connect(url).get()))
    namePairList    <- parseToUserNameAndProjectTitle(topicDocuments)
    partialData     <- combine(titles, descriptions, urls)
    fullData        =  partialData.zip(namePairList).map(_ concat _)
    _               <- putStrLn(s"full data = $fullData")
    _               <- putStrLn(s"full data length = ${partialData.length}")
    csv             <- ZIO.effect {
                      val file = new java.io.File("data.csv")
                      val writer = CSVWriter.open(file)
                      writer.writeRow(Seq("title", "description", "url", "user_name", "project_title", "stars"))
                      writer.writeAll(fullData)
                    }
  } yield ()


  private def parseToUserNameAndProjectTitle(topicDocuments: List[Document]): Task[List[List[String]]] = Task.effect {
    topicDocuments.map(_.body)
      .map(_.select("h3"))
      .filter(_.hasClass("f3 color-fg-muted text-normal lh-condensed"))
      .map(_.select("a"))
      .map { tag =>
        val namePair = tag.text.split(" ")

        List(namePair(0), namePair(1))
      }
  }

  private def parseToTitles(paragraphs: List[Element]): Task[List[String]] = Task.effect(paragraphs.map(_.text))

  private def parseToHyperLinks(body: Element): Task[List[Element]] = Task.effect {
    val linkTag = "a"
    val linkClassName = "no-underline flex-1 d-flex flex-column"

    parseElementByTagAndClassName(body)(linkTag, linkClassName)
  }

  private def parseElementByTagAndClassName(body: Element)(tag: String, className: String): List[Element] = {
    body.select(tag).stream()
      .filter(_.hasClass(className))
      .collect(Collectors.toList)
      .asScala
      .toList
  }


}
