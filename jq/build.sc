import $ivy.`org.jsoup:jsoup:1.13.1`
import $ivy.`org.xerial:sqlite-jdbc:3.34.0`

import java.sql._
import org.jsoup.Jsoup

import ammonite.ops._
import scala.util.Using
import scala.collection.JavaConverters._


val info = """
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>CFBundleIdentifier</key>
	<string>jq</string>
	<key>CFBundleName</key>
	<string>jq - Manual</string>
	<key>DocSetPlatformFamily</key>
	<string>jq</string>
	<key>isDashDocset</key>
	<true/>
  <key>dashIndexFilePath</key>
  <string>index.html</string>
</dict>
</plist>
""".trim

val ddl = """
DROP TABLE IF EXISTS searchIndex;
CREATE TABLE searchIndex(id INTEGER PRIMARY KEY, name TEXT, type TEXT, path TEXT);
CREATE UNIQUE INDEX anchor ON searchIndex (name, type, path);
"""

case class Entry(name: String, tpe: String, path: String)

@main
def create(version: String = "1.6", wd: Path = pwd): Unit = {
  val url  = s"https://stedolan.github.io/jq/manual/v${version}/"
  val resp = requests.get(url)
  val html = Jsoup.parse(resp.text, url)

  val dir       = wd / "jq.docset"
  val infoPlist = dir / "Contents" / "Info.plist"
  val db        = dir / "Contents" / "Resources" / "docSet.dsidx"
  val index     = dir / "Contents" / "Resources" / "Documents" / "index.html"

  rm! dir
  write(infoPlist, data = info, createFolders = true)

  val sections = html.select("#navcolumn ul.nav a").asScala.toList
    .map(elem => Entry(elem.text(), "Section", "index.html" + elem.attr("href")))
  val functions = html.select("section").asScala.toList
    .collect {
      case elem if elem.selectFirst("h3") != null =>
        Entry(elem.selectFirst("h3").text(), "Function", "index.html#" + elem.attr("id"))
    }
    .distinct

  List("#navcolumn", ".navbar", "footer").foreach(selector =>
    html.select(selector).remove())
  write(index, data = html.toString, createFolders = true)

  Using(DriverManager.getConnection(s"jdbc:sqlite:${db}")) { conn =>
    Using(conn.createStatement)(_.executeUpdate(ddl)).get
    Using(conn.prepareStatement("INSERT INTO searchIndex(name, type, path) VALUES (?,?,?)")) { stmt =>
      (sections ++ functions).foreach { entry =>
        stmt.setString(1, entry.name)
        stmt.setString(2, entry.tpe)
        stmt.setString(3, entry.path)
        stmt.addBatch()
      }
      stmt.executeBatch()
    }.get
  }.get
}

