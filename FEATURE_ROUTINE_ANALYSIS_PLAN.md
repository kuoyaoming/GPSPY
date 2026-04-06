# 日常行為紀錄分析 (Routine Activity Analysis) 開發規劃

## 1. 功能概述

新增「日常行為紀錄分析」功能，讓使用者可以追蹤每天在不同地點停留的時間比例（住家、公司、戶外停留、移動中）。

## 2. 核心功能與需求規格

### 2.1 狀態定義與判斷邏輯
系統將依據使用者的地理位置與 Google Activity Recognition API，將行為歸類為以下四種狀態：

*   **移動中 (Moving)**：Activity Recognition API 判定為 `IN_VEHICLE`, `ON_BICYCLE`, `ON_FOOT`, `RUNNING`, `WALKING` 等非靜止狀態。
*   **住家 (Home)**：
    *   Activity Recognition API 判定為 `STILL` (靜止)。
    *   當前位置距離「設定的住家位置」在指定的半徑範圍內（預設 100 公尺，可自訂）。
*   **公司 (Work)**：
    *   Activity Recognition API 判定為 `STILL` (靜止)。
    *   當前位置距離「設定的公司位置」在指定的半徑範圍內（預設 100 公尺，可自訂）。
*   **戶外停留 (Outdoor Stay)**：
    *   Activity Recognition API 判定為 `STILL` (靜止)。
    *   當前位置 **不** 在住家與公司的半徑範圍內。

### 2.2 防抖機制 (Debouncing)
為了避免短暫停留（例如等紅綠燈）被誤判為狀態切換，設定 **5 分鐘** 的防抖閾值。
*   必須 **連續 5 分鐘** 維持 `STILL` 狀態，才正式切換進入「住家」、「公司」或「戶外停留」狀態。
*   在未滿 5 分鐘前，如果原本是「移動中」，則繼續計算為「移動中」。

### 2.3 地點設定 (Location Settings)
*   **設定方式**：使用 Android 內建的 `Geocoder` API，讓使用者輸入地址並轉換為經緯度（無須額外付費 API）。
*   **參數設定**：允許使用者自訂判斷半徑（預設 100m）。

### 2.4 權限與背景執行
*   **所需權限**：
    *   `ACTIVITY_RECOGNITION`：用於判斷靜止或移動狀態。
    *   `ACCESS_BACKGROUND_LOCATION`：用於在背景持續追蹤位置。
    *   `POST_NOTIFICATIONS`：用於顯示前台服務常駐通知。
*   **前台服務 (Foreground Service)**：
    *   為了長期穩定追蹤且不被系統休眠砍殺，必須啟用 Foreground Service。
    *   **使用者確認**：首次啟用此功能需使用者同意，並顯示常駐通知（例如「日常行為分析執行中...」）。
    *   若日後權限被取消或不足，App 需透過本地通知提醒使用者重新授權。

### 2.5 數據呈現與匯出
*   **UI 呈現**：使用圓餅圖 (Pie Chart) 顯示「單日」這四種狀態的時間分配比例。提供日期切換功能（單日為單位）。
*   **資料儲存**：狀態切換事件與持續時間將另外記錄於 Room 資料庫中。
*   **匯出格式**：維持現有架構，提供將單日分析結果匯出為 **JSON 格式**的獨立檔案。UI 上維持獨立的匯出按鈕（如：匯出軌跡 GPX、匯出分析 JSON）。

## 3. 系統架構修改規劃

### 3.1 UI 層 (Jetpack Compose)
1.  **設定頁面 (Settings Screen)**：新增住家/公司地址輸入框與半徑設定滑桿，並實作 `Geocoder` 轉換邏輯。
2.  **分析頁面 (Analysis Screen)**：新增 Tab 或獨立頁面顯示當日圓餅圖與時間分配。
3.  **權限請求流程**：在啟用功能時，新增請求 Activity Recognition 與 Background Location 的對話框與流程。

### 3.2 Domain 層 (Use Cases & Models)
1.  **Activity State Model**：定義狀態列舉與紀錄實體（Start Time, End Time, State）。
2.  **Location Settings Model**：儲存住家、公司座標與半徑設定。
3.  **State Machine Logic**：實作包含 5 分鐘防抖機制的狀態切換判斷邏輯 (Use Case)。

### 3.3 Data 層 (Repository & Room)
1.  **Room Schema**：新增 `RoutineAnalysis` Table，紀錄狀態切換的時間戳記與類型。
2.  **DataStore**：新增偏好設定，儲存功能啟用狀態、住家/公司座標與半徑。
3.  **JSON Export**：實作將指定日期的 `RoutineAnalysis` 資料轉換為 JSON 格式並匯出的邏輯。

### 3.4 Service 層 (Foreground & APIs)
1.  **ActivityRecognitionClient**：整合 Google Play Services 的 Activity Recognition API，監聽 `PendingIntent` 或迴圈輪詢最新狀態。
2.  **Tracking Service**：修改現有的 `LocationService` (或建立新的 `RoutineAnalysisService`)，以支援常駐通知與長時間低功耗運行。並確保 GPS 取樣頻率在靜止與移動時可做動態調整，以優化耗電。

## 4. 開發階段劃分

*   **Phase 1**：資料庫 Schema、DataStore 設定、UI 設定頁面 (Geocoder 地址轉換)。
*   **Phase 2**：Activity Recognition API 整合、權限請求、背景 Service 常駐邏輯。
*   **Phase 3**：核心防抖邏輯與狀態判定實作。
*   **Phase 4**：UI 圓餅圖呈現、單日資料查詢、JSON 匯出功能。
