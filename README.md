# jef4j

## はじめに

jef4j は、富士通株式会社のメインフレームで使われていた JEF 漢字コード体系と Unicode を相互変換するための　Java 用 Charset ライブラリです。

JEF漢字コード体系は、Unicodeの整備も進んだ今となっては消え行く定めにあるものではありますが、企業の基幹系ではいまだ多数のメインフレームが稼働しているため、既存システムからのデータ移行は現在でも重要な技術です。

1979 年に策定された、と聞くと古臭いと思われるかもしれません。しかし、実際には最新の Unicode でも異体字領域を使わないとカバーできないくらい様々な漢字が収録されており、漢字集合としては決して古びたものではありません。富士通社のメインフレームは多くの公共機関でも使われており、その意味で日本の基盤を支える文字コード体系のひとつと言うことができるかもしれません。

残念なことに JEF コード体系は、富士通株式会から正式に公開されたものがありません。そのため、jef4j は次の文献およびインターネット上で手に入るツールを元に作成しています。

- [JHT(ホスト連携ツール)SIMPLE版](http://www.vector.co.jp/soft/winnt/util/se094205.html)： Windows-31J の範囲は概ねこのツールからマッピングを生成しています。ただし、Windows-31J は JIS83 をベースにしているため、Unicode へのマッピングには不適当な部分があります。jef4j では、字形重視で Unicode にマッピングするなど多々変更を加えています。
- FACOM JEF 文字コード索引辞書 (1980/第二版): 拡張漢字についてはこの資料を元にしています。現在では入手困難なため、図書館にて（書籍貸出不可のため）閲覧および著作権法で許可された範囲でコピーしたものを参照し、独自で Unicode へのマッピングを作成しています。なお、最新の規格は1987年発売の第三版に記載されていますが、福岡大学図書館にしか置かれておらず、現時点では確認できていません。
- [Linkexpress 運用ガイド コード変換型の対応表(EUC(S90)系/JEF-EBCDIC系)](http://software.fujitsu.com/jp/manual/manualfiles/m140001/j2x15930/12z200/unyo05/unyo0424.html)： JIS の字体変更の影響で追加された文字のマッピングがここに記載されています。jef4j では、Unicode との変換を目的としているため字形重視でマッピングしています。
- [Canon F359 ユーザーズガイド](http://cweb.canon.jp/manual/lasershot/pdf/crmes-f359.pdf)：JEF 拡張非漢字のマッピングはこのガイドを元に作成しています。Unicode に該当する記号が存在しないため、変換できない部分があります。

JEF標準漢字領域は、JIS78 に準拠しているため大きく異なっているということはないはずですが、JEF拡張漢字領域に関しては第三版で追加された文字の情報が入手できておらず、完全にマッピング出来ている不明な状態です。必要なマッピングをご連絡いただければ登録したいと思いますので、Issue や Pull Request にてご連絡をお願いします。

[現時点でのマッピングはこちら](https://hidekatsu-izuno.github.io/jef4j/docs/mappings.html)からご覧いただけます。

なお、このプロダクトは富士通株式会社とはまったく関係ありませんので問い合わせはご遠慮ください。

## JEF漢字コード体系の構造

JEF漢字コード体系自体は JIS78 (JIS C 6226:1978)に基づく JIS非漢字、JIS第一水準、第二水準からなる標準のコード域と拡張漢字、拡張非漢字と呼ばれる追加のコード域からなる 2 バイトのコード体系です。

|カテゴリ|コード域|
|-----|------|
|全角空白|0x4040|
|拡張漢字/拡張非漢字|0x41A1～0x7FFE|
|利用者定義|0x80A1～0xA0FE|
|標準漢字/標準非漢字|0xA1A2～0xFEFE|

PC で一般的に使われる SHIFT_JIS や EUC-JP なども JIS コード体系に基づいてるため標準漢字/非漢字領域に関しては概ね互換性があるのですが、 これらの文字コードは JIS83 (JIS X 0208:1983) に基づいているため、一部の字体やコード域に違いがあります。JIS83 での JIS78 からの字体変更に際し、その翌年に策定された JEF84 では、従来のコード値はそのままに、変更後の字体は拡張漢字領域に配置したため、上位互換性が保たれています（ただし、JEF79 の時点で JIS83 と同一字体になっている文字もあり、差分だけが追加される形になっているようです）。

|JEF|元規格など|
|-----|------|
|JEF79|JIS78 + 拡張漢字/非漢字|
|JEF84|JIS78 + 拡張漢字/非漢字 + JIS83追加/変更字体|

拡張漢字/拡張非漢字の領域には、JIS補助漢字やJIS第三、第四水準にも含まれない多数の文字が収録されています。そのため PC から利用する場合には、追加の拡張漢字サポート製品を購入する必要があります。そのため、JEF漢字のサポートをうたうツールであっても、拡張漢字/拡張非漢字については、外字登録が必要など限定的なサポートしかされないことが多いようです。

また、JEF漢字コード体系には半角英数や半角カナは含まれません。そのため、メインフレームでよく使われる EBCDIC と併用して使われます。

EBCDICのコード体系としては、IBM-EBCDIC 相当のもの、半角カナを追加した日立製作所 EBCDIK 相当のもの、US-ASCII との互換性を重視したマッピングの3種類が存在します。これらはいずれも1バイトのコード体系ですが、SHIFT_JIS や EUC-JP とは異なり JEF 漢字コードとコード域が重なるため両方を同時に使うことはできません。そのため、シフトコードを使って1バイトコード体系と2バイトコード体系の切り替えを行います。

|カテゴリ|コード値|備考|
|-----|------|----|
|シフトイン(12pt)|0x28||
|シフトイン(9pt)|0x38|エンコード時は 0x28 を使用します。|
|シフトアウト|0x29||

## Unicode にマッピングできない文字の扱いについて

日本の漢字コード体系の基本となっている JIS 漢字コード体系は、文字の標準化だけを行っており、字形の違いは表示上の問題であるとの方針で作成されています。そのため、例示される字形はたびたび変更されています。Unicode も同様にすべての字形にユニークな番号を振るのではなく、包摂基準に基き複数の字体を CJK 統合漢字に統合しています。そのため、いわゆる異体字については、異体字セレクタという別の仕組みを用いて表現する必要があります。

例えば、JEF では、二点しんにょうの「辻󠄃(C4D4)」と一点しんにょうの「辻󠄂(67A5)」が別のコードとして登録されていますが、Unicode ではいずれも「辻(U+8FBB)」にマッピングされます。異体字セレクタを用いると、「辻󠄃(U+8FBB\_E0102)」および「辻󠄂(U+8FBB\_E0103)」という形で意図した字形で表示することができるようになります。

jef4jでは、デコード(JEF → Unicode)時については、この異体字セレクタも含めた形での変換も可能になっています。 （Java NIO Charset API の制約のため、エンコードに関しては対応できませんでした）

現時点で、第二版までの範囲については、拡張非漢字（変体仮名と記号の一部）を除き、すべてのコードがマッピングした状態となっています。

## インストール

Maven Central Repository から取得できます。

```xml
<dependency>
  <groupId>net.arnx</groupId>
  <artifactId>jef4j</artifactId>
  <version>0.7.1</version>
</dependency>
```

## 使い方

jef4j 用に特別なAPIが用意されているわけではありません。クラスパスに jar ファイルを追加すると Java 標準 API を通して使用することができます。

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
|x-Fujitsu-JEF|JEF漢字のみのコード表です。Unicodeにマッピングできない異体字は標準字体に縮退変換されます。|
|x-Fujitsu-JEF-EBCDIC|1バイト領域の英小文字用 EBCDIC と2バイト領域の JEF 漢字をシフトイン/シフトアウトで切り替えます。Unicodeにマッピングできない異体字はCJK統合漢字に縮退変換されます。|
|x-Fujitsu-JEF-EBCDIK|1バイト領域のカナ文字用 EBCDIC と2バイト領域の JEF 漢字をシフトイン/シフトアウトで切り替えます。Unicodeにマッピングできない異体字はCJK統合漢字に縮退変換されます。|
|x-Fujitsu-JEF-ASCII|1バイト領域のASCII互換用 EBCDIC と2バイト領域の JEF 漢字をシフトイン/シフトアウトで切り替えます。Unicodeにマッピングできない異体字はCJK統合漢字に縮退変換されます。|
|x-Fujitsu-JEF-HanyoDenshi|JEF漢字のみのコード表です。（デコード時のみ）IVD汎用電子の異体字セレクタを合わせて出力します。Unicodeにマッピングできない異体字はCJK統合漢字に縮退変換されます。|
|x-Fujitsu-JEF-HanyoDenshi-EBCDIC|1バイト領域の英小文字用 EBCDIC と2バイト領域の JEF 漢字をシフトイン/シフトアウトで切り替えます。（デコード時のみ）IVD汎用電子の異体字セレクタを合わせて出力します。|
|x-Fujitsu-JEF-HanyoDenshi-EBCDIK|1バイト領域のカナ文字用 EBCDIC と2バイト領域の JEF 漢字をシフトイン/シフトアウトで切り替えます。（デコード時のみ）IVD汎用電子の異体字セレクタを合わせて出力します。|
|x-Fujitsu-JEF-HanyoDenshi-ASCII|1バイト領域のASCII互換用 EBCDIC と2バイト領域の JEF 漢字をシフトイン/シフトアウトで切り替えます。（デコード時のみ）IVD汎用電子の異体字セレクタを合わせて出力します。|

変換に失敗した場合の置換文字としては、半角/全角空白が使用されます。Windows-31J など他の文字コードでは'?'が使用されますが、シフトイン/シフトアウトでの切り替えがあるため、どちらでも有効な文字として解釈できる半角空白（JEF/EBCDIC併用時は半角空白２文字）に置換しています。

## ライセンス

Apache License 2.0 で配布します。

## 変更履歴

- 2018/3/11 version 0.7.1: 字形に対する Unicode 値が不明であった２文字（JEF:6AC6、JEF:48C2）が CJK 統合漢字拡張 B および F に存在することがわかったためマッピングを修正しました。
- 2018/3/11 version 0.7.0: 「FACOM JEF 文字コード索引辞書 (1980/第二版)」 に基づき拡張漢字領域のコードを追加し、多数のマッピング不備を修正しました。
