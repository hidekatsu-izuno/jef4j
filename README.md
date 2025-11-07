[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.arnx/jef4j/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.arnx/jef4j)

# jef4j

## はじめに

jef4j は、富士通株式会社のメインフレームで使われていた JEF 漢字コード体系、株式会社日立製作所のメインフレームで使われていた KEIS、日本電気株式会社のメインフレームで使われていた JIPS といったレガシーな文字コードを Unicode を相互変換するための Java 用 Charset ライブラリです。

漢字のマッピングは、目検・手作業にて行っておりますので、誤りを発見した場合には Issue や Pull Request にてご連絡をお願いします。

このプロダクトは富士通株式会社、株式会社日立製作所、日本電気株式会社とはまったく関係ありませんので各社への問い合わせはご遠慮ください。

## JEF漢字コードとは

JEF (JAPANESE PROCESSING EXTENDED FEATURE) は、富士通株式会社のメインフレームで使われていた漢字コード体系です。Unicodeの整備も進んだ今となっては消え行く定めにあるものではありますが、企業の基幹系ではいまだ多数のメインフレームが稼働しているため、既存システムからのデータ移行は現在でも重要な技術です。

1979 年に策定された、と聞くと古臭いと思われるかもしれません。しかし、実際には最新の Unicode でも異体字セレクタを使わないとカバーできない様々な漢字が収録されており、漢字集合としては決して古びたものではありません。富士通社のメインフレームは多くの公共機関でも使われており、その意味で日本の基盤を支える文字コード体系のひとつと言うことができるかもしれません。

[現時点でのマッピングはこちら](https://hidekatsu-izuno.github.io/jef4j/docs/mappings.html)からご覧いただけます。

### JEF 漢字コード体系の構造

JEF漢字コード体系自体は JIS78 (JIS C 6226:1978)に基づく JIS非漢字、JIS第一水準、第二水準からなる標準のコード域と拡張漢字、拡張非漢字と呼ばれる追加のコード域からなる 2 バイトのコード体系です。

|カテゴリ|コード域|
|-----|------|
|全角空白|0x4040|
|拡張漢字/拡張非漢字|0x41A1～0x7FFE|
|利用者定義|0x80A1～0xA0FE|
|標準漢字/標準非漢字|0xA1A2～0xFEFE|

※下位バイトが A0、FF となる領域は使用されません。

JEF 漢字コードの標準漢字/非漢字領域については、PC でも一般的に使われている SHIFT_JIS や EUC-JP 同様 JIS コード体系に基づいてるため概ね互換性があるのですが、 JEF 漢字コード策定後は改定時に参考字体が変更されているため、一部の字体に差異が発生しています。（Cyber Librarian「[JIS X 0208およびJIS X 0213の字形・字体の変更点](https://www.asahi-net.or.jp/~ax2s-kmtn/ref/jisrev.html)」が詳しい）
具体的には、JEF 漢字コードは、JIS78 (JIS C 6226:1978) に基づいているのに対し、SHIFT_JIS などは、JIS83 (JIS X 0208:1983) に基づいているため、一部の字体やコード域に違いがあります。これは、JIS83 策定の翌年に策定された改訂版の JEF84 では、従来の領域には変更を加えず、変更された字体を拡張漢字領域に配置し上位互換性が保つ方針をとったためと思われます（ただし、理由は不明ですが、JEF79 の時点で JIS83 の字体になっていた文字が存在するためすべての文字が追加されているわけではないようです）。

|JEF|元規格など|
|-----|------|
|JEF79|JIS78 + 拡張漢字/非漢字|
|JEF84|JIS78 + 拡張漢字/非漢字 + JIS83追加/変更字体|

拡張漢字/拡張非漢字の領域には、JIS第三、第四水準でもカバーできない多数の文字が収録されています。そのため PC から利用する場合には、追加の拡張漢字サポート製品を購入する必要があります。このような事情から、JEF漢字のサポートをうたうツールであっても、拡張漢字/拡張非漢字については、外字登録が必要など限定的なサポートしかされないことが多いようです。

### 1 バイト文字との併用

JEF漢字コード体系には半角英数や半角カナは含まれません。そのため、メインフレームではよく使われる EBCDIC という1バイト文字体系と併用して使われます。

EBCDICのコード体系としては、IBM-EBCDIC 相当のもの、半角カナを追加した日立製作所 EBCDIK 相当のもの、US-ASCII との整合性を重視したマッピングの3種類が存在します。これらはいずれも 1 バイトのコード体系ですが、SHIFT_JIS や EUC-JP とは異なり JEF 漢字コードとコード域が重なるため両方を同時に使うことはできません。このため、シフトコードを使って1バイトコード体系と2バイトコード体系の切り替えを行います。

|コード名|カテゴリ|コード値|備考|
|-------|--------|-------|----|
|Kシフト|シフトアウト(12pt)|0x28||
|K1シフト|シフトアウト(9pt)|0x38|エンコード時は 0x28 を使用します。|
|Aシフト|シフトイン|0x29||

### Unicode にマッピングできない文字の扱い

日本の漢字コード体系の基本となっている JIS 漢字コード体系は、文字の標準化だけを行っており、字形の違いは表示上の問題であるとの方針で作成されています。そのため、例示される字形はたびたび変更されています。Unicode も同様にすべての字形にユニークな番号を振るのではなく、包摂基準に基き複数の字体を CJK 統合漢字に統合しています。そのため、いわゆる異体字については、異体字セレクタという別の仕組みを用いて表現する必要があります。

例えば、JEF では、二点しんにょうの「辻󠄃(C4D4)」と一点しんにょうの「辻󠄂(67A5)」が別のコードとして登録されていますが、Unicode ではいずれも「辻(U+8FBB)」にマッピングされます。異体字セレクタを用いると、「辻󠄃(U+8FBB\_E0102)」および「辻󠄂(U+8FBB\_E0103)」という形で意図した字形で表示することができるようになります。

jef4jでは、デコード(JEF → Unicode)時については、異体字セレクタを使うことで変換が可能になっています。

現時点で、以下のコードを除き、すべてのコードが正しくマッピングされた状態となっています。

- IVD汎用電子の異体字セレクタでも表現できない異体字２文字（顛、儲）⇒ 字形が近い異体字にマッピング
- 変体仮名　⇒　詳細不明な一文字を除き、字形が近い異体字にマッピング
- 一部のグラフ図形など特殊記号 ⇒　FFFD にマッピング
- 一部の縦書き文字　⇒　FFFD にマッピング

#### 利用者定義領域の取り扱い

JEF の利用者定義文字 3102 文字は、Unicode 私的利用領域 E000～EC1D にマッピングされます。

### JEF に関する参考文献

- FACOM JEF 文字コード索引辞書 (1987年/第三版): 拡張漢字についてはこの資料を元にしています。現在では入手困難なため、図書館にて（書籍貸出不可のため）閲覧および著作権法で許可された範囲でコピーしたものを参照し、独自で Unicode へのマッピングを作成しています。
- [JHT(ホスト連携ツール)SIMPLE版](http://www.vector.co.jp/soft/winnt/util/se094205.html)： Windows-31J の範囲は概ねこのツールからマッピングを生成しています。ただし、Windows-31J は JIS83 をベースにしているため、Unicode へのマッピングには不適当な部分があります。jef4j では、字形重視で Unicode にマッピングするなど多々変更を加えています。
- [Linkexpress 運用ガイド コード変換型の対応表(EUC(S90)系/JEF-EBCDIC系)](http://software.fujitsu.com/jp/manual/manualfiles/m140001/j2x15930/12z200/unyo05/unyo0424.html)： JIS の字体変更の影響で追加された文字のマッピングがここに記載されています。jef4j では、Unicode との変換を目的としているため字形重視でマッピングしています。
- [Canon F359 ユーザーズガイド](http://cweb.canon.jp/manual/lasershot/pdf/crmes-f359.pdf)：JEF 拡張非漢字のマッピングはこのガイドを元に作成しています。Unicode に該当する記号が存在しないため、変換できない部分があります。

## KEIS 漢字コードとは

KEIS (KANJI EXTENDED INFORMATION PROCESSING SYSTEM) は株式会社日立製作所のメインフレームで使われていた漢字コード体系です。

### KEIS 漢字コード体系の構造

KEIS 漢字コード体系は、JIS78 (JIS C 6226:1978)に基づく KEIS78 と JIS83 (JIS X 0208:1983) に基づく KEIS-83 の２つから構成される２バイトコード体系です。JEF や JIPS が JIS83 での字体変更に対し既存コード体系の拡張（字体変更された文字を別コードで追加）する方式を選んだのに対し、KEIS は漢字コード体系自体が変更されています。

KEIS83、はその後、IBM拡張文字への対応などが行われた KEIS90、JIS2004に対応した KEIS2004 へと拡張されたようです。

### KEIS 漢字コード体系の構造

|カテゴリ|コード域|
|-----|------|
|全角空白|0x4040|
|拡張文字セット3|0x59A1～0x80FE|
|ユーザ定義文字|0x81A1～0xA0FE|
|基本文字セット(非漢字)|0xA1A1～0xACFE|
|システムユース文字、書式制御文|0xADA1～0xAFFE|
|基本文字セット(漢字)|0xB0A1～0xCEFE|
|拡張文字セット1|0xD1A1～0xFEFE|

基本文字セットは JIS 第一水準、拡張文字セット1は JIS 第二水準の JIS コード体系に 0x8080 加算したものであるため、結果的に EUC-JP と互換性があります（特に字体変更のない KEIS-83 はそのままです）。拡張文字セット2は存在しません。

拡張文字セット3 については、情報がなく対応できておりません。情報をお持ちの方は<a href="https://github.com/hidekatsu-izuno/jef4j/issues">issues までご連絡</a>いただけますと幸いです。

※富士通社資料には 0x9FA1～0x9FD8 に拡張文字セット3が割り当てられているという記載があるが、日立社資料にそれを裏付ける資料は見つかっていない。

### KEIS と EBCDIC/EBCDIK の併用

KEIS 漢字コード体系には半角英数や半角カナは含まれませんので EBCDIC/EBCDIK と併用して使われます。EBCDICはASCIIとは異なり8bit体系でありコードが重なるため、シフトコードを使って1バイトコード体系と2バイトコード体系の切り替えを行う必要があります。

|コード名|カテゴリ|コード値|
|--------|--------|-------|
|全角シフト|シフトアウト|0x0A42|
|半角シフト|シフトイン|0x0A41|

#### KEIS のユーザ定義文字の取り扱い

KEIS のユーザ定義文字は、Unicode 私的利用領域には以下のようにマッピングされます。

- 0x81A1～0xA0FE (3008文字): E000～EBBF

#### KEIS に関する資料

- [Hitachi Virtual Storage Platform 5000 Cross-OS File Exchange ユーザーズガイド](https://itpfdoc.hitachi.co.jp/manuals/4047/40471JU64_SVOSRF987/40471JU64.pdf)
- [Interstage Charset Manager Standard Edition V9 使用手引書 C.8 KEISコード系の概要](https://software.fujitsu.com/jp/manual/manualfiles/m200002/b1wd0741/14z200/b0741-c-08-00.html)
- [文字コード表 日本語EUC(euc-jp)](http://charset.7jp.net/euc.html)
- [OpenTP1 Version 7 マニュアル](https://itpfdoc.hitachi.co.jp/manuals/3000/30003D5851/CLNT0276.HTM)
- [PRINT DATA EXCHANGE - Form Designer マニュアル](https://itpfdoc.hitachi.co.jp/manuals/3020/30203P0360/PDEF0203.HTM)

## JIPS 漢字コードとは

JIPS (Japanese Information Processing System) とは日本電気株式会社のオフコンで使われていた漢字コード体系です。
JIPS には、JISコード配列をそのまま採用した JIPS(J) と JISコードを EBCDIC 範囲にマッピングした JIPS(E) があります。

### JIPS 漢字コード体系の構造

JIPS 漢字コード体系は 2バイトの各バイトを2分割ずつにした4領域で定義され、それぞれ G0～G3集合と呼ばれます。
そのうち G0集合については、JIS78 (JIS C 6226:1978) の JISコード配列がそのまま配置されます。

|カテゴリ|コード域|
|-----|------|
|G0集合|0x2121～0x7E7E|
|G1集合|0xA1A1～0xFEFE|
|G2集合|0xA121～0xFE7E|
|G3集合|0x21A1～0x7EFE|

なおG0、G1集合のうち下記範囲は外字領域として定義されています。

|カテゴリ|コード域|
|-----|------|
|G0外字領域|0x7421～0x7E7E|
|G1外字領域|0xE0A1～0xFEFE|

G1～G3集合 については、情報がなく部分的な対応に留まっています。情報をお持ちの方は<a href="https://github.com/hidekatsu-izuno/jef4j/issues">issues までご連絡</a>いただけますと幸いです。

### JIPS と EBCDICカナ文字 の併用

JIPS 漢字コード体系には半角英数や半角カナは含まれませんので EBCDICカナ文字と併用して使われます。EBCDICはASCIIとは異なり8bit体系でありコードが重なるため、シフトコードを使って1バイトコード体系と2バイトコード体系の切り替えを行う必要があります。

|コード名|カテゴリ|コード値|
|--------|--------|-------|
|全角シフト|シフトアウト|JIPS(J)：0x1A70、JIPS(E)：0x3F75|
|半角シフト|シフトイン|JIPS(J)：0x1A71、JIPS(E)：0x3F76|

#### JIPS の外字領域の取り扱い

JIPSの利用者定義文字は各集合の後半部分に配置されます。複数のパートに分かれるため、Unicode 私的利用領域に以下のようにマッピングされます。

- G0集合 0x7421～0x7D7E (1034文字): E000～E409
- G1集合 0xE0A1～0xFEFE (2914文字): E40A～EF6B

### JIPS に関する参考文献

- [Interstage Charset Manager Standard Edition V9 使用手引書 C.7 JIPSコード系の概要](https://software.fujitsu.com/jp/manual/manualfiles/m200002/b1wd0741/14z200/b0741-c-07-00.html)
- [オフコン練習帳 ２バイト文字系の文字コード体系](https://offcom.jp/modules/amanual/index.php/ouyou/mojicode/moji_code10.html)
- [歴博 REKIHAKU 小形克宏「Windows外字と、その互換性をめぐる争い」](http://kanji.zinbun.kyoto-u.ac.jp/~yasuoka/publications/2013-09Rekihaku.pdf)
- [PC-9801プログラマーズBible](https://dn790009.ca.archive.org/0/items/PC9801Bible/PC-9801Bible_%E6%9D%B1%E4%BA%AC%E7%90%86%E7%A7%91%E5%A4%A7%E5%AD%A6%20%281%29.pdf)
- [NEC WebOTX Manual V10.1 (第7版)](https://docs.nec.co.jp/sites/default/files/webotx_manual_v101/WebOTX/101/html/serviceintegration/olfadapter/ref/library/convert.html?utm_source=chatgpt.com)

## 異体字セレクタとは

漢字は元々、人間が紙に書いていたこともあり、「高」と「髙」のように同じ文字であっても字体に揺れがあります。
このようなものを異体字と呼びます。

UNICODEでは原則として異体字は取り扱わず、下記の例外を除き、同じ文字はひとつのコードにマッピングする方針で設計されました。

- ソース分離規則: 源泉となった体系（SHIFT_JISなど）で異なる文字として扱われているものは、相互運用性を確保するため異なるコードを付与する。
- 申請上の誤り: 本来同じ文字として扱われるべきものが異なるコードとして申請され受理されてしまった。

しかしながら、印刷など字体の違いを明確にしたいニーズはあり、UNICODE ではこの用途として異体字セレクタが提供されています。
この異体字セレクタを使うと標準字体の後にヒントを埋め込む形で字体を明確化することができます。JEF などホスト系の拡張漢字は UNICODE の標準字体の範囲に収まらないため、この異体字セレクタを使う必要があります。

異体字セレクタには包摂基準の違いにより次の2種類があります。包摂基準とは、止め跳ねの違いのように、同じ文字とするか異なる文字とするかは解釈を統一するための基準です。

- Adobe-Japan1
- 汎用電子情報交換環境整備プログラム（Hanyo-Denshi）＋文字情報基盤整備事業（Moji_Joho）

前者は従来 PDF や Adobe 製品に使われてきたフォントの区別用途で、後者は公共系システムの文字基盤のために整備されたものです。
jef4j では v0.11.0 で Adobe-Japan1 異体字セレクタへの出力にも対応しました。

### Adobe-Japan1 異体字セレクタ対応について

本対応については、安岡孝一さんの成果をベースにしてマッピングを作成しています。

- [Adobe-Japan1-6とMJ文字図形名の対応](http://kanji.zinbun.kyoto-u.ac.jp/~yasuoka/publications/2017-03-10.pdf)

## インストール

Maven Central Repository から取得できます。

```xml
<dependency>
  <groupId>net.arnx</groupId>
  <artifactId>jef4j</artifactId>
  <version>0.11.0</version>
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

### 富士通系文字セット

|文字セット名|説明|
|----------|----|
|x-Fujitsu-EBCDIC|富士通 EBCDIC (英小文字)|
|x-Fujitsu-EBCDIK|富士通 EBCDIC (カナ文字)|
|x-Fujitsu-ASCII|富士通 EBCDIC (ASCII)|
|x-Fujitsu-JEF|富士通 JEF。異体字セレクタは出力されません。|
|x-Fujitsu-JEF-Reversible|JEF のうち、相互変換（JEF⇔Unicode）が可能なコードのみに限定したものです。主にデータ移行用途です。|
|x-Fujitsu-JEF-HanyoDenshi|富士通 JEF。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-Fujitsu-JEF-AdobeJapan1|富士通 JEF。Adobe-Japan1の異体字セレクタを合わせて出力します。主にPDF用途です。|
|x-Fujitsu-JEF-EBCDIC|富士通 EBCDIC (英小文字) と JEF をシフトコードで切り替えながら出力します。異体字セレクタは出力されません。|
|x-Fujitsu-JEF-HanyoDenshi-EBCDIC|富士通 EBCDIC (英小文字) と JEF をシフトコードで切り替えながら出力します。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-Fujitsu-JEF-AdobeJapan1-EBCDIC|富士通 EBCDIC (英小文字) と JEF をシフトコードで切り替えながら出力します。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|
|x-Fujitsu-JEF-EBCDIK|富士通 EBCDIC (カナ文字) と JEF をシフトコードで切り替えながら出力します。異体字セレクタは出力されません。|
|x-Fujitsu-JEF-HanyoDenshi-EBCDIK|富士通 EBCDIC (カナ文字) と JEF をシフトコードで切り替えながら出力します。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-Fujitsu-JEF-AdobeJapan1-EBCDIK|富士通 EBCDIC (カナ文字) と JEF をシフトコードで切り替えながら出力します。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|
|x-Fujitsu-JEF-ASCII|富士通 EBCDIC (ASCII) と JEF をシフトコードで切り替えながら出力します。異体字セレクタは出力されません。|
|x-Fujitsu-JEF-HanyoDenshi-ASCII|富士通 EBCDIC (ASCII) と JEF をシフトコードで切り替えながら出力します。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-Fujitsu-JEF-AdobeJapan1-ASCII|富士通 EBCDIC (ASCII) と JEF をシフトコードで切り替えながら出力します。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|

変換に失敗した場合の置換文字としては、半角/全角空白が使用されます。Windows-31J など他の文字コードでは'?'が使用されますが、シフトイン/シフトアウトでの切り替えがあるため、どちらでも有効な文字として解釈できる半角空白（JEF/EBCDIC併用時は半角空白２文字）に置換しています。

### 日立系文字セット（ベータ）

|文字セット名|説明|
|----------|----|
|x-Hitachi-EBCDIC|日立 EBCDIC|
|x-Hitachi-EBCDIK|日立 EBCDIK|
|x-Hitachi-KEIS78|日立 KEIS78。異体字セレクタは出力されません。|
|x-Hitachi-KEIS78-HanyoDenshi|日立 KEIS78。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-Hitachi-KEIS78-AdobeJapan1|日立 KEIS78。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|
|x-Hitachi-KEIS78-EBCDIC|日立 EBCDIC と KEIS78 をシフトコードで切り替えながら出力します。異体字セレクタは出力されません。|
|x-Hitachi-KEIS78-HanyoDenshi-EBCDIC|日立 EBCDIC と KEIS78 をシフトコードで切り替えながら出力します。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-Hitachi-KEIS78-AdobeJapan1-EBCDIC|日立 EBCDIC と KEIS78 をシフトコードで切り替えながら出力します。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|
|x-Hitachi-KEIS78-EBCDIK|日立 EBCDIK と KEIS78 をシフトコードで切り替えながら出力します。異体字セレクタは出力されません。|
|x-Hitachi-KEIS78-HanyoDenshi-EBCDIK|日立 EBCDIK と KEIS78 をシフトコードで切り替えながら出力します。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-Hitachi-KEIS78-AdobeJapan1-EBCDIK|日立 EBCDIK と KEIS78 をシフトコードで切り替えながら出力します。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|
|x-Hitachi-KEIS83|日立 KEIS83/90。異体字セレクタは出力されません。|
|x-Hitachi-KEIS83-HanyoDenshi|日立 KEIS83/90。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-Hitachi-KEIS83-AdobeJapan1|日立 KEIS83/90。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|
|x-Hitachi-KEIS83-EBCDIC|日立 EBCDIC と KEIS83/90 をシフトコードで切り替えながら出力します。異体字セレクタは出力されません。|
|x-Hitachi-KEIS83-HanyoDenshi-EBCDIC|日立 EBCDIC と KEIS83/90 をシフトコードで切り替えながら出力します。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-Hitachi-KEIS83-AdobeJapan1-EBCDIC|日立 EBCDIC と KEIS83/90 をシフトコードで切り替えながら出力します。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|
|x-Hitachi-KEIS83-EBCDIK|日立 EBCDIK と KEIS83/90 をシフトコードで切り替えながら出力します。異体字セレクタは出力されません。|
|x-Hitachi-KEIS83-HanyoDenshi-EBCDIK|日立 EBCDIK と KEIS83/90 をシフトコードで切り替えながら出力します。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-Hitachi-KEIS83-AdobeJapan1-EBCDIK|日立 EBCDIK と KEIS83/90 をシフトコードで切り替えながら出力します。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|

### NEC系文字セット（ベータ）

|文字セット名|説明|
|----------|----|
|x-NEC-EBCDIK|NEC EBCDICカナ文字|
|x-NEC-JIPSJ|NEC JIPS(J)。異体字セレクタは出力されません。|
|x-NEC-JIPSJ-HanyoDenshi|NEC JIPS(J)。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-NEC-JIPSJ-AdobeJapan1|NEC JIPS(J)。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|
|x-NEC-JIPSJ-EBCDIK|NEC EBCDICカナ文字 と NEC JIPS(J) をシフトコードで切り替えながら出力します。異体字セレクタは出力されません。|
|x-NEC-JIPSJ-HanyoDenshi-EBCDIK|NEC EBCDICカナ文字 と NEC JIPS(J) をシフトコードで切り替えながら出力します。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-NEC-JIPSJ-AdobeJapan1-EBCDIK|NEC EBCDICカナ文字 と NEC JIPS(J) をシフトコードで切り替えながら出力します。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|
|x-NEC-JIPSE|NEC JIPS(E)。異体字セレクタは出力されません。|
|x-NEC-JIPSE-EBCDIK|NEC EBCDICカナ文字 と NEC JIPS(E) をシフトコードで切り替えながら出力します。異体字セレクタは出力されません。|
|x-NEC-JIPSE-HanyoDenshi|NEC JIPS(E) 。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-NEC-JIPSE-AdobeJapan1|NEC JIPS(E) 。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|
|x-NEC-JIPSE-HanyoDenshi-EBCDIK|NEC EBCDICカナ文字 と NEC JIPS(E) をシフトコードで切り替えながら出力します。異体字セレクタにはIVD汎用電子のものが使用されます。|
|x-NEC-JIPSE-AdobeJapan1-EBCDIK|NEC EBCDICカナ文字 と NEC JIPS(E) をシフトコードで切り替えながら出力します。異体字セレクタにはAdobe-Japan1のものが使用されます（主にPDF用途）。|

## ライセンス

Apache License 2.0 で配布します。

文字コードのマッピングファイルについては CC-O (Public Domain 相当) にて配布いたします。

- src/test/resources/*.json

## 変更履歴

- 2025/8/13 version 0.11.0: 
  - Adobe Japan-1 の IVS を使った変換に対応しました。
  - データ移行を想定し逆変換（JEF→Unicode→JEF）が可能なコードのみに限定した「x-Fujitsu-JEF-Reversible」を追加しました。
- 2025/8/8 version 0.10.2: 
  - 変体仮名のデコード時にエラーが出る問題を修正しました。
  - 迩、珊、悗を縮退する際に標準漢字に戻るよう縮退変換の順序を修正しました。
  - 武を字母とする変体仮名 71E0 を U+1B0D0 にマッピングしました。
- 2025/5/19 version 0.10.1: x-Fujitsu-(EBCDIC|EBCDIK|ASCII|JEF) 指定時にシフトコードがエラーとならず無視される問題を修正しました。
- 2025/5/10 version 0.10.0: 汎用電子指定時のデコードにて異体字セレクタを出力できるよう改善しました。従来と同じ動作にしたい場合は、デコード時に「-HanyoDenshi」が含まれていない文字セットを指定してください。また、「哨󠄁」の異体字セレクタが誤っていたため修正しました。
- 2021/10/20 version 0.9.3: 「鯵鰺篭籠」のマッピングが間違っていることが判明したため修正しました。
- 2021/4/27 version 0.9.2: 「棒」のマッピングが漏れていることが判明したため追加しました。
- 2019/9/20 version 0.9.0: FACOM JEF 文字コード索引辞書 (1987年/第三版) に基づきマッピング等の見直しを行いました。
- 2018/3/27 version 0.7.2: 拡張非漢字領域にあった漢字部品用コードのマッピングが漏れていたため追加しました。
- 2018/3/11 version 0.7.1: 字形に対する Unicode 値が不明であった２文字（JEF:6AC6、JEF:48C2）が CJK 統合漢字拡張 B および F に存在することがわかったためマッピングを修正しました。
- 2018/3/11 version 0.7.0: 「FACOM JEF 文字コード索引辞書 (1980/第二版)」 に基づき拡張漢字領域のコードを追加し、多数のマッピング不備を修正しました。

## 参考文献

- [uCosminexus Interschema ユーザーズガイド 付録K.4　EBCDIC／EBCDIKのコード表](https://itpfdoc.hitachi.co.jp/manuals/3020/30203J3820/ISUS0268.HTM)