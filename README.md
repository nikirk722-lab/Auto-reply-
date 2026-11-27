# AutoReply Jack 🚀 — V3 (Ultra Edition)

SMS বা যেকোনো চ্যাট অ্যাপে মেসেজ এলে তুমি রিপ্লে না দিলে অ্যাপটা নিজে থেকে রিপ্লে দেবে — **সবকিছু তুমি নিজে কাস্টাইজ করতে পারো**।

## 🧠 V3 — নতুন Advanced features

| Feature | কী করে |
|---|---|
| **Smart rules** | মেসেজের ভেতরের keyword দেখে আলাদা রিপ্লে + আলাদা delay। যেমন keyword `কোথায়` → "_আমি বাইরে আছি, শীঘ্রই ফিরব_"। `re:where\|কোথায়` লিখলে regex-ও চলে। Target (নম্বর/অ্যাপ/উক্তি) দিতে পারো — শুধু ওই ব্যক্তির ক্ষেত্রে rule চলবে। প্রথম ম্যাচ করা rule জয়ী |
| **Random delay** | নির্দিষ্ট নম্বরের বদলে min–max-এর মধ্যে এলোমেলো সময় → মানুষের মতো লাগে |
| **DRY RUN mode** | কিছু পাঠায় না, শুধু log-এ দেখায় "এটা হতো" — নিরাপদে পরীক্ষা |
| **Skip saved contacts** | তোমার কন্টাক্ট লিস্টের লোকেদের অটো-রিপ্লে যায় না (READ_CONTACTS অনুমতি চাইবে) |
| **Respect DND** | ফোনে DND চালু থাকলে চুপ থাকে |
| **Max replies/day** | একই প্রাপককে দিনে সর্বোচ্চ কতবার রিপ্লে দেবে — স্প্যাম বন্ধ |
| **Reply sent notification** | রিপ্লে গেলে ছোট্ট নোটিফিকেশন দেখা যায় |
| **SMS sent/delivery report** | SMS আসলেই গেল কি না — log-এ `sent OK` / `delivered` দেখা যায় |
| **Share log** | পুরো log এক বাটনে কপি/শেয়ার করা যায় |

## ✨ features (V2)

| Feature | কী করে |
|---|---|
| **Custom delay** | ফিক্সড ৫ মিনিট নেই — ৫ সেকেন্ড থেকে ২৪ ঘণ্টা, সেকেন্ডে লিখো |
| **Select apps** | একটা বাটন চাপলে **ফোনের সব ইনস্টল করা অ্যাপ** checkbox-সহ দেখা যায়; Messenger, IMO, WhatsApp ছাড়াও যেকোনো অ্যাপ বাছতে পারো। সার্চও আছে |
| **Per-app rules** | প্রতি অ্যাপের জন্য **আলাদা রিপ্লে মেসেজ + আলাদা delay** সেট করো |
| **Reply method** | `auto` (নোটিফিকেশনের Reply বাটন, নাহলে accessibility) / শুধু notification / শুধু accessibility |
| **Quiet hours** | যেমন ২২:০০ → ০৭:০০; এই সময়ে কোনো অটো-রিপ্লে যাবে না |
| **Screen-off only** | তুমি ফোন ব্যবহার করছ (স্ক্রিন অন) — তখন চুপ থাকবে |
| **Cooldown** | একই প্রাপককে বারবার রিপ্লে দেবে না; নির্ধারিত সময়ের পরেই আবার |
| **Number filters** | শুধু নির্দিষ্ট নম্বরে রিপ্লে • অথবা নির্দিষ্ট নম্বর এড়িয়ে যাওয়া |
| **Dual SIM** | যে SIM-এ মেসেজ আসে, সেই SIM দিয়েই রিপ্লে যায় |
| **TEST button** | সাথে সাথে রিপ্লে পাঠিয়ে সবকিছু যাচাই করো |
| **Battery button** | ব্যাটারি অপটিমাইজেশন বন্ধ করে (অনেক ফোনে এটা না করলে alarm মেরে দেয়) |
| **Status + Stats + Log** | কোন permission চালু আছে, কতটা রিপ্লে গেছে, কোথায় ব্যর্থ হয়েছে — সব দেখা যায় |
| **Boot aware** | ফোন রিস্টার্ট হলেও অ্যাপ প্রস্তুত থাকে |

## 📲 APK ইনস্টল (প্রথমবার)

1. `/sdcard/Download/AutoReply-debug.apk` — আগের ভার্শন থাকলে **আনইনস্টল** করে নিও (signature বদলায়)।
2. APK-তে ট্যাপ → **Install unknown apps** অন → Install।

## ⚙️ সেটআপ (একবারই)

1. **Grant SMS permissions** → SMS অনুমতি দিও।
2. **Open Notification access** → AutoReply-র টগল অন করো।
3. **Grant notification permission** (Android 13+)।
4. **Enable Accessibility** → AutoReply service অন করো (Reply বাটন না থাকা অ্যাপের জন্য)।
5. **Battery: allow unrestricted** → অনুমতি দিও — **এটা খুব জরুরি**।
6. যদি "Restricted setting" দেখায়: Settings → Apps → AutoReply → ⋮ → **Allow restricted settings** → তারপর আবার টগল অন করো।

> স্ট্যাটাসে ৪টাই ✓ দেখলে সব ঠিক।

## 🛠️ কাস্টাইজেশন

- **Default reply / delay** — প্রথম দুই বক্সে লিখে **Save settings** চাপো। Delay সেকেন্ডে (যেমন `30` = ৩০ সেকেন্ড, `3600` = ১ ঘণ্টা)।
- **Select apps** — সব অ্যাপের লিস্ট আসবে; যেগুলোতে অটো-রিপ্লে চাই সেগুলো টিক দিও → Done।
- **Per-app reply & delay** — প্রতি বেছে নেওয়া অ্যাপের নিচে আলাদা মেসেজ ও delay লিখো (ফাঁকা রাখলে default চলবে)।
- **Reply method** — `auto` রাখাই ভালো; ইচ্ছে হলে নির্দিষ্ট method বাছো।
- **Quiet hours / Screen-off / Cooldown / Number filter** — নিজের মতো সেট করে Save।

### 🧠 Smart rules ব্যবহার
**Smart rules** বাটন → **+ Add rule**:
- **Target** — নম্বর, package name বা conversation title (ফাঁকা = সবার জন্য)
- **Keyword** — মেসেজে যে শব্দ থাকলে রিপ্লে বদলাবে (যেমন `কোথায়`, `call`, `re:where|কোথায়`)
- **Reply** — ওই ক্ষেত্রে যে মেসেজটা যাবে
- **Delay** — সেকেন্ড (ফাঁকা = অ্যাপ/ডিফল্ট delay)
- **Rule active** টিক — বন্ধ করতে পারো

Save করার পর যেকোনো মেসেজে log-এ `[rule matched]` দেখা যাবে।

## ✅ পরীক্ষা

1. **TEST** বক্সে নম্বর লিখে বাটন চাপো → সাথে সাথে রিপ্লে যাবে কি না দেখো।
2. অন্য ফোন থেকে SMS পাঠিয়ো → নির্ধারিত delay-র পর রিপ্লে যাবে (তুমি নিজে রিপ্লে দিলে যাবে না)।
3. কিছু না হলে **Refresh log** চাপো — `Alarm set → Alarm FIRED → sent/FAILED` দেখে বুঝতে পারবে।

## 📁 ফাইল

| ফাইল | কাজ |
|---|---|
| `MainActivity.kt` | সব সেটিংস UI, permission, test |
| `AppPickerActivity.kt` | সব ইনস্টল করা অ্যাপ দেখে বাছাই |
| `RulesActivity.kt` | প্রতি অ্যাপের জন্য আলাদা reply/delay |
| `Settings.kt` | সব কনফিগ + log + stats + filter |
| `SmsReceiver.kt` | ইনকামিং SMS ধরে শিডিউল |
| `MessageNotificationListener.kt` | চ্যাট নোটিফিকেশন পড়ে শিডিউল |
| `ReplyScheduler.kt` | Exact alarm (Doze-সহ) |
| `AlarmReceiver.kt` | রিপ্লে পাঠায় + quiet/cooldown/filter চেক |
| `AutoReplyAccessibilityService.kt` | Reply বাটন না থাকা অ্যাপে টাইপ + Send |
| `BootReceiver.kt` | রিস্টার্টের পর প্রস্তুত |

## 🔧 APK বানানো (পরে চাইলে)

PC-তে Android Studio → **Build → Build APK(s)**, অথবা GitHub Actions (`.github/workflows/build-apk.yml`)। APK পাবে `app/build/outputs/apk/debug/app-debug.apk`।

## ⚠️ সীমাবদ্ধতা

- কিছু অ্যাপ কাস্টম ইনপুট ব্যবহার করে → accessibility fallback ব্যর্থ হতে পারে (log-এ দেখা যাবে)।
- Android 10+ এ ব্যাকগ্রাউন্ড থেকে অ্যাপ খোলা কখনো ব্লক হয় → ফোনের "Start from background" অনুমতি দিও।
- রিস্টার্টে আগের শিডিউলগুলো মুছে যায়; নতুন মেসেজে আবার সেট হয়।

---
Generated by **Big Pickle** 🤖 · সোর্স: `~/AutoReply` · APK: `/sdcard/Download/AutoReply-debug.apk`
