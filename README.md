## 現在チーム制作中
🛠️ 1. 開発を進める上での共通ルール・注意点
ブランチ戦略（Git運用）

main（または master）に直接コードを書き込まず、必ず機能ごとのフィーチャーブランチ（例: feature/product, feature/inventory 等）を切って作業する。

作業の終わりや、キリの良いところまで進んだら、必ずこまめにCommit ＆ Pushする（「誰がどこまでやったか」を可視化するため）。

「動かない状態」で放置して離脱しない

不定期で休むメンバーがいるため、途中の状態でPushする場合は、コメントに「ここが未完成」「ビルドエラー中」などを残すか、動く状態まで戻してコミットする。

HTML/CSSやJSの置き場所を統一する

既存のHTMLテンプレートやCSS、ご自身が担当するJSファイルの位置（src/main/resources/static/ や templates/内）のパスを変更する場合は、チーム内で必ず共有する。

# StockApp（生活必需品・在庫管理アプリ）
家庭やチームで生活必需品の在庫数や使用期限を共有・管理し、買い忘れや切れを防ぐためのSpring Bootアプリケーションです。

## 主な機能
- **在庫・期限管理**: 商品ごとの在庫数、使用期限、アラート閾値を管理。
- **アラート通知・買い物リスト連携**: 「在庫切れ」「残り僅か」「期限切れ」「期限間近」の判定を行い、ホーム表示や買い物リストへの自動追加をサポート。
- **排他制御・同時実行制御**: 買い物リスト操作時の競合を防ぐ悲観的ロック（`PESSIMISTIC_WRITE`）の導入。
- **購入完了・履歴管理**: チェック済み商品の一括購入（在庫自動加算・リスト削除・購入履歴のスナップショット生成）、購入日ごとのグルーピング表示。
- **論理削除対応**: データを物理削除せず、`isDeleted` フラグによる論理削除を採用。
- **グループ（家族）管理**: 家族やチーム単位でデータを共有。

## 使用技術（Tech Stack）
- **Language**: Java 25
- **Framework**: Spring Boot (4.1.2)
- **Backend / Web**: Spring Web MVC, Spring Boot Validation
- **Database / ORM**: Spring Data JPA, H2 Database / MySQL
- **Template Engine**: Thymeleaf
- **Build Tool**: Maven
- **Utilities**: Lombok, Spring Boot DevTools
- **Version Control**: Git / GitHub

## データベース設計（テーブル概要）
- **`products` (商品)**: 在庫のメイン情報（商品名、在庫数、使用期限、閾値、通知判定、各種外部キー、論理削除フラグ）
- **`families` (グループ/家族)**: ユーザーや商品のグループ分け
- **`users` (ユーザー)**: アプリ利用者情報
- **`categories` (分類)**: 食品・日用品などのカテゴリ
- **`units` (個数単位)**: 個、本、パックなどの単位
- **`icons` (アイコン)**: 商品に紐づくアイコン画像
- **`shopping_list_items` (買い物リスト)**: 買う予定のリスト（商品参照、購入数量、チェック状態）
- **`purchase_histories` (購入履歴)**: 購入時の記録（商品名スナップショット、購入数量、購入日）

## チーム開発の進め方・方針
- **リポジトリ層の方針**: Spring Data JPAの基本機能（派生クエリメソッドなど）を活かし、可読性とシンプルさを重視した設計。
- **サービス層の責務分離**: トランザクション境界を明確にし、ビジネスロジック・整合性チェック・排他制御をService層に集約。
- **命名規則・コーディング規約**: チームメンバー間で共通のルールを意識し、保守性の高いコードを目指す。

## 開発ロードマップ & 今後の実装予定
本プロジェクトは、実用的な生活必需品・在庫管理アプリを目指し、以下のステップで開発を進めています。

- [x] **基盤設計・データベース設計**
  - エンティティ設計（Product, Family, Category, Unit, Icon, ShoppingListItem, PurchaseHistory 等）
  - リレーションシップおよび論理削除（`isDeleted`）の定義
- [x] **【バックエンド】リポジトリ層の構築**
  - `ProductRepository`, `ShoppingListItemRepository`, `PurchaseHistoryRepository` 等の構築
  - 派生クエリによる論理削除除外・並び順指定（ソート）の実装
- [x] **【バックエンド】ビジネスロジック層の構築（Service層）**
  - `ShoppingListService`: 在庫・期限連動による自動追加、手動追加、排他制御（悲観的ロック）、一括購入処理
  - `PurchaseHistoryService`: スナップショット保存、購入日別グルーピング処理（LinkedHashMap）
- [ ] **【バックエンド】コントローラー層の構築（進行中）**
  - 画面ごとの責務分担（Home, Product, Inventory, ShoppingList, PurchaseHistory）
  - HTML連携用エンドポイントおよび受け渡しパラメータ（Form/DTO）の実装
- [ ] **【フロントエンド】HTML/UIの実装（進行中）**
  - Thymeleafを用いた画面レイアウト構築（shopping/list.html, history/list.html 等）
  - フォーム送信・非同期通信（チェック状態更新、数量変更）の画面連携
- [ ] **テスト・品質担保（進行中）**
  - `@SpringBootTest` と `@Transactional` を用いた Service層・Repository層のDB連携単体テスト（JUnit 5 / AssertJ）
- [ ] **結合・機能調整**
  - バックエンドとフロントエンドを連携させた画面遷移・データの動作確認