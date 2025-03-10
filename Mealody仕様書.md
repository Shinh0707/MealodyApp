### 作者
平山 心

### アプリ名
Mealody (ミロディー)
Meal+Melody

#### コンセプト
楽曲ストリーミングサービスのようにレストランを検索・管理できるアプリ

#### こだわったポイント
- 現在地周辺のレストラン探索機能
- エリア別検索で様々な検索条件を設定可能
- お気に入りや閲覧履歴の管理機能
- ノート機能によるレストラン整理システム
- シンプルかつ見やすいUI設計

#### デザイン面
- 店舗の詳細表示する際には、店舗のサービスがひと目見てわかるようにした
- 検索結果で店舗が沢山出てくるが、ジャンルごとに色分けして見やすくした
- Hotpepperのレストラン検索アプリよりシンプルなデザインになるように心がけた
- ロゴの背景色とアプリの色に統一感があるようにした

### 該当プロジェクトのリポジトリ URL
https://github.com/Shinh0707/MealodyApp

## 開発環境
### 開発環境
Android Studio

### 開発言語
Kotlin

## 動作対象端末・OS
### 動作対象OS
Android 8.0 (API 26) 以上

## 開発期間
5日間

## アプリケーション機能
### 機能一覧
- **ホーム画面**: ユーザー独自の項目表示、最近閲覧したレストラン、エリア情報表示
- **検索機能**: 
  - キーワード検索（店名、住所、電話番号等）
  - エリア検索（大エリア、中エリア、小エリア）
  - 現在地周辺検索（距離範囲指定可能）
  - 詳細条件検索（禁煙席、駐車場、バリアフリー等の条件設定）
- **検索結果表示**: カルーセル表示でレストラン一覧を閲覧
- **店舗詳細表示**: レストランの詳細情報表示
- **お気に入り機能**: レベル別（3段階）のお気に入り登録
- **ノート機能**: レストランを独自のノートにカテゴリ分け
- **ライブラリ管理**: ノートとお気に入りレストランの一元管理

### 画面一覧
- **ホーム画面**: アプリの入り口、閲覧履歴とエリア選択
- **検索画面**: 検索条件設定
- **検索結果画面**: レストラン一覧表示
- **近隣検索画面**: 現在地周辺のレストラン表示
- **レストラン詳細画面**: 選択したレストランの詳細情報
- **ライブラリ画面**: ノートとお気に入りの管理
- **ノート詳細画面**: 特定ノート内のレストラン管理

### 使用しているAPI・ライブラリなど
- **ホットペッパーグルメサーチAPI**: レストラン情報取得
- **Jetpack Compose**: UI構築
- **Kotlin Coroutines**: 非同期処理
- **Dagger Hilt**: 依存性注入
- **Room Database**: ローカルデータ保存
- **Retrofit**: APIクライアント
- **Google Maps API**: 地図表示・位置情報取得
- **Coil**: 画像読み込み

### 現状の課題
1. **検索結果の店舗詳細表示の問題**:
   - 以前検索した結果がキャッシュされて残る
   
2. **現在地からの距離での絞り込み問題**
   - 距離によるフィルタリングは実装した。
   - 距離を伸ばしたあとの結果をどう再取得するかを悩み、実装できていない。

### 今後の展望
- **サジェスト機能**: ノート・お気に入りに登録されている店舗の特徴をベクトル化して類似度計算してサジェスト表示したい（一番したかったこと）
- **UIの改善**: 色だけでなくアイコンや背景画像でのユニバーサルデザイン強化
- **検索プリセット機能の実装**: よく使う検索条件を保存
- **クーポン・特集情報の表示**: ホーム画面での表示
- **Google Maps連携強化**: 
  - 「地図から選択」機能の実装
  - 地図表示から「現在地で検索」「このエリアで検索」機能
- **ノート管理の改善**: ノート削除時の警告表示
- **設定機能の追加**: アプリ設定の充実
- **ユーザー情報管理**: サーバー連携による情報保存（将来的に）

### 技術面でアドバイスしてほしいこと
- Androidアプリでのデータ管理方法
- キャッシュの方法

### 自己評価
全体的に中途半端な作りになってしまった。共通化できそうな部分も中途半端にしか共通化できておらず、無理やりつなげている箇所も多い。デザインも、使いやすさは微妙で、時折重いことがあるのも微妙。ただ、コンセプト自体はあまり悪くないと思っている。