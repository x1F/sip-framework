# Markdown image embedding test

This is a test file for the [markdown unit test](../../java/de/ikor/sip/cloud/core/util/MarkdownResourceReaderTest.java).

## External image embedding
Here is an external image, that should not be embedded via base64-stream:

![](https://upload.wikimedia.org/wikipedia/commons/7/7a/Karte_Odenwaldexpress.png)

### Internal image embedding 
Here is the internal image, which should still show:

![Bahnhof](img/OdenwaldexpressMudau.jpg)

### Broken image embedding
Next up is a broken internal link, which should not lead to an `IOException`:

![Broken Image Link](/this/file/does/not/exist.jpg?raw=true)

[//]: # (Commented text type 1)
<!-- Commented text type 2 -->
