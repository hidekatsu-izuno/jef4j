# jef4j

## はじめに

jef4j は、富士通株式会社のメインフレームで使われていた JEF 漢字コード体系と Unicode を相互変換するための　Java 用 Charset ライブラリです。

JEF漢字コード体系は、Unicodeの整備も進んだ今となっては消え行く定めにあるものではありますが、企業の基幹系ではいまだ多数のメインフレームが稼働しており、既存システムからのデータ移行は重要な技術です。

1979 年に策定されたと聞くと古臭いと思われるかもしれません。しかし、実際には最新の Unicode でも異体字領域を使わないとカバーできない様々な漢字が収録されており、漢字集合としては決して古びたものではありません。富士通社のメインフレームは多くの公共機関でも使われており、その意味で日本の基盤を支える文字コード体系のひとつと言うことができるかもしれません。

残念なことに JEF コード体系は、富士通株式会から正式に公開されていません（書籍が存在するようですが、現在では入手困難です）。このライブラリの作成にあたり、Unicode へのマッピングは以下のようなインターネット上で入手可能なツールや文献だけを元に作成しています。

- [JHT(ホスト連携ツール)SIMPLE版](http://www.vector.co.jp/soft/winnt/util/se094205.html)：Windows-31J の範囲は概ねこのツールからマッピングを生成しています。ただし、Windows-31J は 83JIS をベースにしているため、Unicode へのマッピングには不適当な部分があります。jef4j では、字形重視でマッピングするように変更してあります。
- [Linkexpress 運用ガイド コード変換型の対応表(EUC(S90)系/JEF-EBCDIC系)](http://software.fujitsu.com/jp/manual/manualfiles/m140001/j2x15930/12z200/unyo05/unyo0424.html)： JIS の字体変更で影響受ける文字の一部がここに記載されています。jef4j では、Unicode との変換を目的としているため字形重視でマッピングしています。
- [Canon F359 ユーザーズガイド](http://cweb.canon.jp/manual/lasershot/pdf/crmes-f359.pdf)：JEF 拡張非漢字のマッピングはこのガイドを元に作成しています。該当する Unicode の文字が見当たらないため変換できていない部分があります。

JEF標準漢字領域は、78JIS に準拠しているためさほど問題はないのですが、JEF拡張漢字領域に関しては部分的にしか情報がなく、完全にはマッピング出来ていません。必要なマッピングをご連絡いただければ登録したいと重ますので、Issue や Pull Request にてご連絡をお願いします。

[現時点でのマッピングはこちら](docs/mappings.html)を参照してください。

なお、このプロダクトは富士通株式会社とはまったく関係ありませんので問い合わせはご遠慮ください。

## JEF漢字コード体系の構造

JEF漢字コード体系自体は 78JIS(JIS C 6226:1978)に基づく JIS非漢字、JIS第一水準、第二水準からなる標準のコード域と拡張漢字、拡張非漢字と呼ばれる追加のコード域からななる 2 バイトのコード体系です。

|カテゴリ|コード域|
|-----|------|
|全角空白|0x4040|
|拡張漢字/拡張非漢字|0x41A1～0x7FFE|
|利用者定義|0x80A1～0xA0FE|
|標準漢字/標準非漢字|0xA1A2～0xFEFE|

PC で一般的に使われる SHIFT_JIS や EUC-JP なども JIS コード体系に基づいてるため標準漢字/非漢字領域に関しては概ね互換性があるのですが、 これらの文字コードは 83JIS(JIS X 0208:1983) に基づいているため、一部の字体やコード域に違いがあります。JEF漢字コードでは、78JIS から 83JIS での字体変更にあたり互換性を重視し、従来のコード値はそのままにし、新しい字体を拡張漢字領域に配置しています。

拡張漢字/拡張非漢字の領域には、JIS補助漢字やJIS第三、第四水準にも含まれない多数の文字が収録されています。そのため PC から利用する場合には、追加の拡張漢字サポート製品を購入する必要があります。そのため、JEF漢字のサポートをうたうツールであっても、拡張漢字/拡張非漢字については、外字登録が必要など限定的なサポートしかされないことが多いようです。

また、JEF漢字コード体系には半角英数や半角カナは含まれません。そのため、メインフレームでよく使われる EBCDIC と併用して使われます。

EBCDICのコード体系としては、IBM-EBCDIC 相当のもの、半角カナを追加した日立製作所 EBCDIK 相当、US-ASCII との互換性を重視したマッピングの3種類が存在します。これらはいずれも1バイトのコード体系ですが、SHIFT_JIS や EUC-JP とは異なり JEF 漢字コードとコード域が重なるため両方を同時に使うことはできません。そのため、シフトコードを使って1バイトコード体系と2バイトコード体系の切り替えを行います。

|カテゴリ|コード値|備考|
|-----|------|----|
|シフトイン(12pt)|0x28||
|シフトイン(9pt)|0x38|エンコード時は 0x28 を使用します。|
|シフトアウト|0x29||

## インストール

Maven Central Repository から取得できます。

```xml
<dependency>
  <groupId>net.arnx</groupId>
  <artifactId>jef4j</artifactId>
  <version>0.2.0</version>
</dependency>
```

## 使い方

jef4j 用に特別なAPIが用意されているわけではありません。jar ファイルをクラスパスに追加すると、Java 標準 API を通して使用可能です。

- new String(byte[] chars, Charset charset)
- String.getBytes(Charset charset)
- Charset.forName(String charset)

```java
Charset charset = Charset.forName("x-Fujitsu-JEF");
String text = new String(bytes, charset);
byte[] bytes = text.getBytes(charset);
```

指定できる文字セット名は次の通りです。

|文字セット名|説明|
|----------|----|
|x-Fujitsu-EBCDIC|英小文字用 EBCDIC のコード表です。|
|x-Fujitsu-EBCDIK|カナ文字用 EBCDIC のコード表です。|
|x-Fujitsu-ASCII|ASCII互換用 EBCDIC のコード表です。|
|x-Fujitsu-JEF|JEF漢字のみのコード表です。|
|x-Fujitsu-JEF-EBCDIC|1バイト領域の英小文字用 EBCDIC と2バイト領域の JEF 漢字をシフトイン/シフトアウトで切り替えます。|
|x-Fujitsu-JEF-EBCDIK|1バイト領域のカナ文字用 EBCDIC と2バイト領域の JEF 漢字をシフトイン/シフトアウトで切り替えます。|
|x-Fujitsu-JEF-ASCII|1バイト領域のASCII互換用 EBCDIC と2バイト領域の JEF 漢字をシフトイン/シフトアウトで切り替えます。|

変換に失敗した場合の置換文字としては、半角/全角空白が使用されます。Windows-31J など他の文字コードでは'?'が使用されますが、シフトイン/シフトアウトでの切り替えがあるため、どちらでも有効な文字として解釈できる半角空白（JEF/EBCDIC併用時は半角空白２文字）に置換しています。

## ライセンス

Apache License 2.0 で配布します。
