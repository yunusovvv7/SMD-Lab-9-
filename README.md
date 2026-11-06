# Amaliy ish: Korutinalar bilan tanishish (Kotlin / kotlinx-coroutines-core 1.10.2)

Ushbu repository Korutinalar amaliy ishi doirasida bajarilgan 15 ta asosiy va 5 ta qo'shimcha savollarning to'liq va mukammal yechimlarini o'z ichiga oladi.

---

## Suspend qilish va Block qilish o'rtasidagi farq

> [!NOTE]
> **Suspend qilish (Suspending) va Block qilish (Blocking) o'rtasidagi asosiy farqlar:**

```text
1. Block qilish (Blocking):
   - Oqimni (Thread) to'liq band qilib qo'yadi va boshqa vazifalarni bajarishiga yo'l qo'ymaydi.
   - Operatsion tizim darajasida oqim kutish holatida qotib qoladi (masalan, Thread.sleep() yoki tarmoq blokirovkasi).
   - Resurslarni behuda sarflaydi. Agar UI oqimida (Main Thread) ishlatilsa, dastur qotib qoladi va ANR (Application Not Responding) xatoligiga olib keladi.

2. Suspend qilish (Suspending):
   - Oqimni to'xtatib qo'ymaydi (bloklamaydi), balki faqat korutina (Coroutine) bajarilishini vaqtincha to'xtatib turadi (masalan, delay()).
   - Shu vaqt mobaynida asosiy oqim boshqa foydali vazifalarni bajarish uchun erkin qoladi (non-blocking).
   - Vaqt tugagach yoki natija tayyor bo'lgach, korutina o'sha oqimda yoki boshqa oqimda o'z ishini muammosiz davom ettiradi.
```

---

## Asosiy Savollar va Yechimlar (1 - 15)

### 1-savol. Birinchi korutina
- **Kontekst:** Kafeda buyurtma darhol tasdiqlanishi kerak, qahva tayyorlanishi uchun esa 1 soniya kerak bo'ladi. Tasdiqlash qahvani kutmasligi kerak.
- **Natija (Output):**
  ```text
  Order accepted
  Coffee ready
  ```
- **Tushuntirish:** `Order accepted` darhol chop etiladi, chunki ishga tushirilgan korutina `delay(1000L)` orqali 1 soniya to'xtab turadi. Bu asosiy kodga korutinaning tugashini kutmasdan davom etishga imkon beradi.

---

### 2-savol. Uch barista
- **Kontekst:** Uchta barista bitta oqimda bir vaqtning o'zida ichimlik tayyorlaydi: espresso 300 ms, choy 100 ms va latte 200 ms.
- **Natija (delay bilan):**
  ```text
  espresso start
  tea start
  latte start
  tea end
  latte end
  espresso end
  ```
- **Natija (Thread.sleep bilan):**
  ```text
  espresso start
  espresso end
  tea start
  tea end
  latte start
  latte end
  ```
- **Tushuntirish:** `delay` ishlatilganda oqim bloklanmaydi va barcha vazifalar concurrent (navbatma-navbat almashib) ishlaydi. `Thread.sleep` ishlatilganda esa oqim har bir barista tomonidan ketma-ket to'liq bloklanadi, shuning uchun biri tugagandagina keyingisi boshlanadi.

---

### 3-savol. Suspend funksiyalar zanjiri
- **Kontekst:** Kirish (login) jarayoni uchta qadamdan iborat: foydalanuvchini yuklash, parolni tekshirish va profilni ochish. Har bir qadam vaqt oladi va qat'iy ravishda oldingisidan keyin bajarilishi kerak.
- **Natija (Output):**
  ```text
  Step 1: Load user
  Step 2: Check password
  Step 3: Open profile
  ```
- **Tushuntirish:** Suspend qilish kodning tartibini buzmaydi; funksiyalar ketma-ket chaqirilgani sababli (`step1()` ichidan `step2()`, uning ichidan `step3()`) ular aynan shunday ketma-ketlikda bajariladi.

---

### 4-savol. Oddiy koddan suspend funksiyani chaqirish
- **Kontekst:** Dasturchi oddiy funksiyadan foydalanuvchini yuklash uchun suspend funksiyani chaqirdi va loyiha kompilyatsiya qilinmadi.
- **Xatolik xabari (Compile Error):**
  ```text
  Suspend function 'loadUserSuspend' should be called only from a coroutine or another suspend function
  ```
- **Yechimlar:**
  1. `runBlocking { ... }` ichiga o'rash (joriy oqimni bloklaydi).
  2. Scope yordamida ishga tushirish: `GlobalScope.launch { ... }` yoki `CoroutineScope.launch { ... }`.
- **UI Oqim Qoidasi:** Android UI (Main) oqimida aslo `runBlocking` ishlatilmaydi, chunki u asosiy oqimni muzlatib qo'yadi va ANR xatosiga sabab bo'ladi.

---

### 5-savol. Suspend bu oqim (thread) emas
- **Kontekst:** Yosh dasturchi funksiyaga `suspend` kalit so'zini yozish uning ishini avtomatik ravishda boshqa (fon) oqimga ko'chiradi deb o'ylaydi.
- **Natija (Output):**
  ```text
  Before call thread: main @coroutine#1
  Returned thread: main @coroutine#1
  ```
- **Tushuntirish:** Suspend funksiya chaqirilgandan oldin ham, keyin ham oqim nomi `main` bo'lib qoladi. Bu `suspend` o'z-o'zidan fon oqimini yaratmasligini isbotlaydi.
- **Yashirin parametr:** Kompilyator har bir suspend funksiyaga `Continuation` obyektini yashirin parametr sifatida qo'shadi.

---

### 6-savol. O'nta yuklab olish, bitta oqim
- **Kontekst:** Yuklab olish menejerida 10 ta fayl bor, lekin faqat asosiy oqimdan foydalanishga ruxsat etilgan. Barcha yuklab olishlar bir-birini kutmasdan progress qilishi kerak.
- **Natija (Output):**
  ```text
  download 0 started on main @coroutine#1
  download 1 started on main @coroutine#2
  ...
  download 9 started on main @coroutine#10
  download 0 finished
  ...
  download 9 finished
  ```
- **Tushuntirish:** Barcha 10 ta yuklab olish biri tugashidan oldin boshlanadi, chunki `delay` bloklamaydi. Bu **konkurrentlik (concurrency)** deb ataladi (bitta oqimda vazifalar o'rtasida almashib ishlash), parallelizm emas.

---

### 7-savol. Launch qilish tartibi
- **Kontekst:** Ekran yuklanish xabarini chiqaradi, fon qadamini boshlaydi va o'z ishini davom ettiradi.
- **Natija (Output):**
  ```text
  1
  4
  2
  5
  3
  ```
- **Tushuntirish:** `1` chop etiladi, `launch` orqali "2" asinxron rejimda rejalashtiriladi, darhol `4` chop etiladi, `delay(50L)` asosiy blokni to'xtatib turadi, "2" ekranga chiqadi, 50ms dan keyin `5` chiqadi va oxirida 100ms dan so'ng `3` chiqadi.

---

### 8-savol. Ota bola korutinani kutishi
- **Kontekst:** Hisobot vazifasi biriktirmani 200 ms davomida siqadigan sub-vazifani boshlaydi. Hisobot faqat sub-vazifa tugagandagina yakunlangan deb belgilanishi kerak.
- **Natija (Output):**
  ```text
  parent body done
  child done
  report completed
  ```
- **Tushuntirish:** Strukturaviy konkurrentlik (structured concurrency) qoidasiga ko'ra, ota korutina o'zining barcha bola korutinalari to'liq tugashini avtomatik ravishda kutadi.

---

### 9-savol. Ekran yopildi, so'rovlar bekor qilindi
- **Kontekst:** Foydalanuvchi tarmoq so'rovlari ketayotgan paytda ekranni yopadi. Ikkala so'rov ham darhol to'xtab, tozalash (cleanup) xabarini chiqarishi kerak.
- **Natija (Output):**
  ```text
  cleanup 1
  cleanup 2
  end
  ```
- **Tushuntirish:** Ota korutina bekor qilinganda (`cancelAndJoin`), bekor qilish signali barcha bola korutinalarga tarqatiladi va ular darhol to'xtab, o'zlarining `finally` bloklarini bajaradilar.

---

### 10-savol. Profil tayyor
- **Kontekst:** Profil ekrani avatarni (200 ms) va sozlamalarni (100 ms) bir vaqtning o'zida yuklaydi. "Profile ready" xabari ikkalasi yuklangandan keyingina chiqishi kerak.
- **Natija (Output):**
  ```text
  Avatar loaded
  Settings loaded
  Profile ready
  end of main
  after scope coroutine
  ```
- **Tushuntirish:** `coroutineScope` ichidagi barcha bolalar tugaguncha kutib turadi va shundan keyingina "Profile ready" ni chiqaradi. Scope'dan keyin ishga tushirilgan korutina esa asinxron ishlaydi.

---

### 11-savol. Qayta ishlatiladigan hisoblagich
- **Kontekst:** Bir nechta ekranlar bir xil tiklanuvchi hisoblagichni ishga tushiradi. Funksiya darhol qaytishi va yangi korutina chaqiruvchi scope'ga tegishli bo'lishi kerak.
- **Natija (Output):**
  ```text
  launched
  Counter A: 1
  Counter B: 1
  Counter A: 2
  Counter B: 2
  Counter A: 3
  ```
- **Tushuntirish:** `CoroutineScope` ga kengaytma (extension) funksiya sifatida yozilganligi sababli, `launch` chaqiruvchining scope'iga bog'lanadi va receiver orqali ishlaydi.

---

### 12-savol. Darhol qaytadi yoki kutadi
- **Kontekst:** Jamoaga ikkita yordamchi kerak: biri fon greeterni boshlab darhol qaytadi, ikkinchisi bir necha kishiga salom yo'llab, barchasi tugagandagina qaytadi.
- **Natija (Output):**
  ```text
  After startBackgroundGreeter call
  Background hello, Alice
  All greetings initiated
  Hello, Bob
  Hello, Charlie
  After greetAll call
  ```
- **Tushuntirish:** `Job` qaytaruvchi extension darhol qaytadi, `coroutineScope` ishlatgan suspend funksiya esa ichidagi barcha bolalar tugashini kutadi.

---

### 13-savol. Beshta builder
- **Cheat Sheet (Qisqatma jadval):**
  - `launch`: `Job` qaytaradi, oqimni bloklamaydi, bolalarni kutadi.
  - `async`: `Deferred<T>` qaytaradi, oqimni bloklamaydi, bolalarni kutadi.
  - `runBlocking`: Natija qaytaradi, joriy oqimni **bloklaydi**, bolalarni kutadi.
  - `coroutineScope`: Natija qaytaradi, oqimni bloklamaydi (suspend qiladi), bolalarni kutadi.
  - `withContext`: Natija qaytaradi, oqimni bloklamaydi (suspend qiladi), kontekstni almashtiradi.

---

### 14-savol. Job yoki Deferred
- **Kontekst:** Dasturchi hisoblash natijasini (`6 * 7`) olish uchun `launch` ishlatdi va qiymatni ololmadi.
- **Tushuntirish:** `launch` faqat `Job` qaytaradi (natija qiymati yo'q, shuning uchun `await()` mavjud emas). `async` esa `Deferred<T>` qaytaradi va uni `await()` yordamida olish mumkin.

---

### 15-savol. Scope ichidagi uchta eksport
- **Kontekst:** Hisobot uchta faylni bir vaqtda eksport qiladi: X (300 ms), Y (100 ms) va Z (200 ms). "All done" xabari barchasi tugagandan keyin chiqishi kerak.
- **Natija (Output):**
  ```text
  Start
  Y
  Z
  X
  All done
  ```
- **Tushuntirish:** Davomiyligiga qarab Y (100ms), Z (200ms) va X (300ms) tartibda tugaydi. `coroutineScope` uchalasining tugashini kutadi va oxirida "All done" ni chop etadi.

---

## Qo'shimcha Savollar (Extra Questions)

### Extra 1. Delay yoki sleep: o'lchash
- **Natija (Output):**
  - Delay time: ~300ms
  - Sleep time: ~900ms
- **Tushuntirish:** `delay` konkurrent ishlaydi (barchasi bir vaqtda ketib 300ms oladi). `Thread.sleep` esa ketma-ket oqimni bloklaydi (3 * 300 = 900ms).

---

### Extra 2. Uch darajali korutinalar
- **Kontekst:** Scope ikkita bola boshlaydi, ulardan biri nevara (grandchild) boshlaydi.
- **Korutina daraxti (Coroutine Tree):**
  ```text
  coroutineScope (Scope)
   ├── launch (Child 1)
   │    └── launch (Grandchild - waits 200ms)
   └── launch (Child 2 - waits 100ms)
  ```
- **Natija (Output):**
  ```text
  child 2 done
  child 1 done
  grandchild done
  scope done
  ```
- **Tushuntirish:** Child 2 100ms da tugaydi. Child 1 tanasi darhol tugaydi (`child 1 done`). Grandchild esa 200ms dan keyin `grandchild done` ni chiqaradi. `scope done` eng oxirida chiqadi, chunki `coroutineScope` barcha bola va nevara korutinalar to'liq tugashini kutadi.

---

### Extra 3. Scope yo'q, launch yo'q
- **Xatolik:**
  ```text
  Unresolved reference: launch
  ```
- **Yechim:** `runBlocking` orqali scope ochish. `runBlocking` joriy oqimni bloklagan holda korutina scope yaratib beradi va oqimni ushlab turadi.

---

### Extra 4. Kompilyator nimani ko'radi
- **Bytecode imzosi:**
  ```kotlin
  final Object loadUserById(int id, Continuation continuation)
  ```
- **Yashirin parametr va qaytish turi:**
  - Yashirin parametr: `Continuation` (suspend nuqtalarida qayta tiklash uchun).
  - Yangi qaytish turi: `Object` (chunki natija asinxron ravishda `COROUTINE_SUSPENDED` yoki haqiqiy natijani qaytarishi mumkin).

---

### Extra 5. Hammomga salom (Greet everybody)
- **Natija (Output):**
  ```text
  before
  Hello, Ada
  Hello, Linus
  Hello, Grace
  after
  ```
- **Tushuntirish:** Korutinalar o'sib boruvchi pauzalar bilan (`100ms`, `200ms`, `300ms`) ishga tushadi. `coroutineScope` "after" faqat barcha salomlashishlar tugagach chiqishini ta'minlaydi.
