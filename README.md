# jef4j

JEF charset support for Java

## はじめに

JEF は富士通株式会社のメインフレームで使われていた文字コード体系です。Unicodeの整備も進んだ今となっては消え行く定めにある文字コード体系ではありますが、既存システムの資産を移行するにはどうしても必要になるため、今回 Java 用の charset として作成しました。

今回、調べて改めてわかったことですが、JEF 漢字コードは、 1979 年に策定されたからと言って決して古くさい体系ではないということです。JEF 拡張漢字領域まで含めると広範囲にわたって様々な漢字が収録されており、現在の Unicode でも異体字領域まで含めないとマッピングできない文字すら収録されています。おそらく、公共機関の要請もあったものと思われますが、その意味で日本の基盤を支える文字コード体系として今も使い続けられているようです。

残念なことに JEF コード体系は、富士通株式会社外には正式に公開されていません。このライブラリの作成にあたり次のインターネット上のツールや文献を元にマッピングを作成しておりますが、特にJEF拡張漢字領域に関しマッピングが十分に出来ておりません。文字コードの対応をご連絡いただければマージしたいと思いますので、Issue や Pull Request でご連絡をお願いします。

- [JHT(ホスト連携ツール)SIMPLE版](http://www.vector.co.jp/soft/winnt/util/se094205.html) Windows-31J の範囲は概ねこのツールからマッピングを生成しています。ただ、マッピングの一部に不備が見つかったため、その部分だけ修正を加えています。
- [Linkexpress 運用ガイド コード変換型の対応表(EUC(S90)系/JEF-EBCDIC系)] JEF 拡張漢字の一部は JIS の字体変更の影響により一部がここに記載されています。このライブラリでは、Unicode との変換を目的としているため字形重視でマッピングしています。
- [Canon F359 ユーザーズガイド](http://cweb.canon.jp/manual/lasershot/pdf/crmes-f359.pdf) JEF 拡張非漢字のマッピングはこのガイドを元に作成しています。一部 Unicode への適当なマッピング先が見つからないため、変換できないものもあります。

なお、このプロダクトは富士通株式会社とは関係ありませんので問い合わせはご遠慮ください。また、私自身も富士通社のメインフレームを触ったことがなく、汎用機の理解を前提にした質問などを頂いても回答しかねますのでご認識をお願いします。

## インストール

Maven Central Repository から取得できるようになる予定です（現在作業中）。

```xml
<dependency>
  <groupId>net.arnx</groupId>
  <artifactId>jef4j</artifactId>
  <version>0.1.0</version>
</dependency>
```

## 使い方

UTF-8、Windows-31J などの文字コードと同様に new String(byte[] chars, String charset)　や Charset.forName(String charset) の charset として次のいずれかを指定します。

|文字セット名|説明|
================
|x-Fujitsu-ASCII|EBCDIC-ASCII のコード表です。|
|x-Fujitsu-EBCDIC|英小文字用 EBCDIC のコード表です。|
|x-Fujitsu-EBCDIK|カナ文字用 EBCDIC のコード表です。|
|x-Fujitsu-JEF|JEF漢字のみのコード表です。|
|x-Fujitsu-JEF-ASCII|1バイト領域の EBCDIC-ASCII と2バイト領域の JEF 漢字をシフトイン/シフトアウトで切り替えます。|
|x-Fujitsu-JEF-EBCDIC|1バイト領域の英小文字用 EBCDIC と2バイト領域の JEF 漢字をシフトイン/シフトアウトで切り替えます。|
|x-Fujitsu-JEF-EBCDIK|1バイト領域のカナ文字用 EBCDIC と2バイト領域の JEF 漢字をシフトイン/シフトアウトで切り替えます。|

```java
String text = new String(bytes, "x-Fujitsu-JEF");
byte[] bytes = text.getBytes("x-Fujitsu-JEF");
```

## ライセンス

Apache License 2.0 で配布します。