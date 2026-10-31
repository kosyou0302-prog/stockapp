# 🛒 StockApp（生活必需品・在庫管理アプリ）
家庭やチームで生活必需品の在庫数や使用期限を共有・管理し、買い忘れや切れを防ぐためのSpring Bootアプリケーションです。

## 🚀 主な機能
- **在庫・期限管理**: 商品ごとの在庫数、使用期限、アラート閾値を管理。
- **アラート通知・買い物リスト連携**: 「在庫切れ」「残り僅か」「期限切れ」「期限間近」の判定を行い、ホーム表示や買い物リストへの自動追加をサポート。
- **論理削除対応**: データを物理削除せず、`isDeleted` フラグによる論理削除を採用。
- **グループ（家族）管理**: 家族やチーム単位でデータを共有。

## 🛠️ 使用技術（Tech Stack）
- **Backend**: Java, Spring Boot, Spring Data JPA
- **Database**: MySQL / H2 Database（環境に応じて）
- **Template Engine / Frontend**: Thymeleaf (予定)
- **Build Tool**: Maven / Gradle
- **Version Control**: Git / GitHub

## 📂 データベース設計（テーブル概要）
- **`products` (商品)**: 在庫のメイン情報（商品名、在庫数、使用期限、閾値、通知判定、各種外部キー、論理削除フラグ）
- **`families` (グループ/家族)**: ユーザーや商品のグループ分け
- **`users` (ユーザー)**: アプリ利用者情報
- **`categories` (分類)**: 食品・日用品などのカテゴリ
- **`units` (個数単位)**: 個、本、パックなどの単位
- **`icons` (アイコン)**: 商品に紐づくアイコン画像
- **`purchase_histories` (購入履歴)**: 購入時の記録
- **`shopping_lists` (買い物リスト)**: 買う予定のリスト

## 👥 チーム開発の進め方・方針
- **リポジトリ層の方針**: Spring Data JPAの基本機能（クエリメソッドなど）を活かし、可読性とシンプルさを重視した設計。
- **命名規則・コーディング規約**: チームメンバー間で共通のルールを意識し、保守性の高いコードを目指す。

## 🗺️ 開発ロードマップ & 今後の実装予定
本プロジェクトは、実用的な生活必需品・在庫管理アプリを目指し、以下のステップで開発を進めています。

- [x] **基盤設計・データベース設計**
  - エンティティ設計（Product, Family, Category, Unit, Icon 等）
  - リレーションシップおよび論理削除（`isDeleted`）の定義
- [x] **リポジトリ層（データアクセス）の構築**
  - `ProductRepository` の基本構築（論理削除除外の全件取得など）
- [ ] **アラート・通知機能の実装**
  - 在庫切れ・残り僅か・期限切れ・期限間近を判定するカスタムクエリの実装
- [ ] **サービス層・コントローラー層の実装**
  - ビジネスロジックの構築およびREST API / 画面（Thymeleaf）連携
- [ ] **テスト・品質担保**
  - リポジトリ・サービスの単体テスト（JUnit / Mockito）

  erDiagram
    families {
        bigint id PK
        string groupName "グループ名"
    }

    users {
        bigint id PK
        bigint family_id FK "グループID"
        string name "名前"
        string email "メールアドレス"
        string password "パスワード"
    }

    categories {
        bigint id PK
        string category "分類名"
    }

    units {
        bigint id PK
        string unit "単位名"
    }

    icons {
        bigint id PK
        string icon "アイコン名"
    }

    products {
        bigint id PK
        bigint family_id FK "グループID"
        bigint category_id FK "分類ID"
        bigint unit_id FK "単位名ID"
        bigint icon_id FK "アイコンID"
        string name "商品名"
        int stock "在庫数"
        date expiry_date "使用期限"
        int threshold "購入目安"
        boolean is_notify "通知判定"
        int alert_days "期限選択"
        boolean is_deleted "論理削除"
    }

    purchase_histories {
        bigint id PK
        bigint family_id FK "グループID"
        bigint product_id FK "商品ID"
        string name "商品名"
        int quantity "購入数"
        date record_date "記入日"
    }

    shopping_lists {
        bigint id PK
        bigint family_id FK "グループID"
        bigint item_id FK "商品ID"
        int qty "買う予定個数"
    }

    %% リレーションの定義
    families ||--o{ users : "contains"
    families ||--o{ products : "manages"
    families ||--o{ purchase_histories : "has"
    families ||--o{ shopping_lists : "has"
    categories ||--o{ products : "categorizes"
    units ||--o{ products : "uses"
    icons ||--o{ products : "uses"
    products ||--o{ purchase_histories : "recorded_in"
    products ||--o{ shopping_lists : "listed_in"