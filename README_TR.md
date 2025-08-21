# 🚫 AntiEscape - Gelişmiş Minecraft Oyuncu Kontrol Eklentisi

[![GitHub release](https://img.shields.io/github/v/release/xOrcun/AntiEscape?style=flat-square)](https://github.com/xOrcun/AntiEscape/releases)
[![GitHub license](https://img.shields.io/github/license/xOrcun/AntiEscape?style=flat-square)](https://github.com/xOrcun/AntiEscape/blob/main/LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/xOrcun/AntiEscape?style=flat-square)](https://github.com/xOrcun/AntiEscape/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/xOrcun/AntiEscape?style=flat-square)](https://github.com/xOrcun/AntiEscape/network)
[![GitHub issues](https://img.shields.io/github/issues/xOrcun/AntiEscape?style=flat-square)](https://github.com/xOrcun/AntiEscape/issues)
[![GitHub contributors](https://img.shields.io/github/contributors/xOrcun/AntiEscape?style=flat-square)](https://github.com/xOrcun/AntiEscape/graphs/contributors)

> **Discord entegrasyonu, kapsamlı loglama ve çok dilli destek ile gelişmiş Minecraft sunucu oyuncu kontrol ve güvenlik sistemi.**

---

## 🌍 **Language / Dil Seçimi**

### 🇹🇷 **Türkçe Kullanıcılar İçin:**
**Türkçe dokümantasyon için bu dosyayı okuyabilirsiniz.**

### 🇺🇸 **English Users:**
**İngilizce dokümantasyon için [README.md](README.md) dosyasını inceleyin.**

---

## 🌟 **Özellikler**

### 🔒 **Temel Kontrol Sistemi**
- **Oyuncu Kontrolü**: Oyuncuları kontrol altına al ve hareketlerini kısıtla
- **Hareket Önleme**: Tüm oyuncu hareketlerini, ışınlanmalarını ve eylemlerini engelle
- **Komut Engelleme**: Kontrol altındaki oyuncuların komut kullanmasını engelle
- **Sohbet Kontrolü**: Yöneticiler için izole kontrol sohbet sistemi
- **Eşya Düşürme Önleme**: Kontrol altındaki oyuncuların eşya düşürmesini engelle
- **Hasar Koruması**: Kontrol altındaki oyuncuları saldırılardan koru
- **Kendi Kendini Kontrol Etme Önleme**: Oyuncular kendilerini kontrol altına alamaz

### 🚀 **Gelişmiş Kontrol Sistemi**
- **Oturum Yönetimi**: Zaman damgaları ile kontrol oturumlarını takip et
- **Kontrol Geçmişi**: Tüm kontrol eylemlerinin tam geçmişi
- **Oyuncu Notları**: Her oyuncu için not ekle ve yönet
- **İstatistikler**: Kaçış denemelerini ve şüpheli aktiviteleri takip et
- **Zaman Limitleri**: Maksimum kontrol süresi ile otomatik serbest bırakma
- **Bildirimler**: BossBar, Title, ActionBar ve Ses bildirimleri
- **Kaçış Takibi**: Oyuncular kontroldeyken çıktığında kayıt

### 🛡️ **Gelişmiş Güvenlik Sistemi**
- **IP Adresi Kontrolü**: IP adreslerini izle ve kısıtla
- **VPN/Proxy Tespiti**: VPN bağlantılarını tespit et ve engelle
- **Şüpheli Aktivite**: Hızlı hareketleri, komutları ve sohbeti izle
- **Otomatik Ban Sistemi**: Güvenlik ihlalleri için otomatik banlama
- **Beyaz Liste Sistemi**: Güvenilir oyuncular için güvenlik kontrollerini atla
- **Güvenlik Loglama**: Kapsamlı güvenlik olay loglaması
- **Ban Artırımı**: Tekrarlayan ihlaller için ban süresini artır

### 📊 **Kapsamlı Loglama Sistemi**
- **Ayrı Log Dosyaları**: Her olay türü için farklı log dosyaları
- **Yapılandırılabilir Loglama**: Belirli log türlerini aç/kapat
- **Log Rotasyonu**: Otomatik log dosyası rotasyonu ve sıkıştırma
- **Olay Türleri**: Hareketler, komutlar, sohbet, hasar, eşyalar, kontrol, genel
- **Detaylı Bilgi**: Zaman damgaları, oyuncu bilgileri, konum verileri

### 🔗 **Discord Entegrasyonu**
- **Webhook Sistemi**: Discord kanallarına bildirim gönder
- **Özelleştirilebilir Embed'ler**: Tamamen özelleştirilebilir embed mesajları
- **Olay Bildirimleri**: Kontrol başlangıcı/bitişi, kaçışlar, hareketler, komutlar
- **Zengin Formatlama**: Özel renkler, küçük resimler, alanlar ve alt bilgiler
- **Asenkron İşleme**: Sunucuyu bloke etmeyen webhook teslimi

### 🌍 **Çok Dilli Destek**
- **İngilizce ve Türkçe**: Kolay geçiş ile tam dil desteği
- **Yapılandırılabilir Mesajlar**: Dil dosyaları ile tüm mesajlar özelleştirilebilir
- **Kolay Lokalizasyon**: Yeni diller eklemek için basit yapı
- **Otomatik Oluşturma**: Dil dosyaları otomatik olarak oluşturulur

### 📈 **İzleme ve İstatistikler**
- **Gerçek Zamanlı İzleme**: Aktif kontrolleri ve oyuncu durumunu takip et
- **Performans Metrikleri**: Eklenti performansını ve kullanımını izle
- **bStats Entegrasyonu**: Anonim kullanım istatistikleri
- **Güncelleme Kontrolü**: Otomatik sürüm kontrolü ve bildirimler

---

## 🚀 **Hızlı Başlangıç**

### 📋 **Gereksinimler**
- **Minecraft Sunucusu**: 1.16+ (Spigot/Paper/Bukkit)
- **Java**: Java 8 veya üzeri
- **İzinler**: Vault eklentisi (önerilen)

### 📥 **Kurulum**

1. **İndir** [Releases](https://github.com/xOrcun/AntiEscape/releases) sayfasından en son sürümü
2. **Yerleştir** JAR dosyasını `plugins` klasörüne
3. **Yeniden Başlat** sunucunu
4. **Yapılandır** eklentiyi `config.yml` ile
5. **Kur** Discord webhook'larını (isteğe bağlı)

### ⚙️ **Temel Yapılandırma**

```yaml
# Temel ayarlar
prefix: "&6ᴀɴᴛɪᴇѕᴄᴀᴘᴇ &8▸ &7"
language: "tr"
debug: false

# Kontrol konumları
control-location: "none"  # /control set area ile ayarla
control-spawn-location: "none"  # /control set return ile ayarla

# Otomatik ban sistemi
auto-ban:
  enabled: true
  normal-ban:
    duration: "1d"
    reason: "Güvenlik ihlali tespit edildi"
  escape-ban:
    duration: "7d"
    reason: "Kontrol sırasında kaçış denemesi"
  movement-ban:
    enabled: false
    duration: "1h"
    reason: "Kontrol sırasında hareket ihlali"
    max-violations: 3
  ban-command: "tempban %player% %duration% %reason%"
```

---

## 📖 **Komutlar**

### 🔧 **Ana Komutlar**
| Komut | Açıklama | İzin |
|-------|-----------|------|
| `/control help` | Yardım menüsünü göster | `antiescape.general` |
| `/control take <oyuncu>` | Bir oyuncuyu kontrol altına al | `antiescape.general` |
| `/control end <oyuncu> [ban]` | Kontrolü bitir veya oyuncuyu banla | `antiescape.general` |
| `/control chat <join/leave>` | Kontrol sohbetine katıl/ayrıl | `antiescape.general` |

### 📍 **Konum Komutları**
| Komut | Açıklama | İzin |
|-------|-----------|------|
| `/control set area` | Kontrol alanını ayarla | `antiescape.general` |
| `/control set return` | Dönüş konumunu ayarla | `antiescape.general` |
| `/control delete area` | Kontrol alanını sil | `antiescape.general` |
| `/control delete spawn` | Dönüş konumunu sil | `antiescape.general` |

### 🔍 **Gelişmiş Kontrol Komutları**
| Komut | Açıklama | İzin |
|-------|-----------|------|
| `/control history <oyuncu>` | Kontrol geçmişini göster | `antiescape.general` |
| `/control notes <add/clear/list> <oyuncu>` | Oyuncu notlarını yönet | `antiescape.general` |
| `/control stats <oyuncu>` | Oyuncu istatistiklerini göster | `antiescape.general` |

### 🛡️ **Güvenlik Komutları**
| Komut | Açıklama | İzin |
|-------|-----------|------|
| `/control whitelist <add/remove> <oyuncu> [sebep]` | Beyaz listeyi yönet | `antiescape.general` |
| `/control suspicious <oyuncu> [clear]` | Şüpheli aktiviteyi kontrol et | `antiescape.general` |
| `/control violations <oyuncu> [clear]` | İhlal sayısını kontrol et | `antiescape.general` |
| `/control ip <ip> [oyuncu]` | IP bilgilerini kontrol et | `antiescape.general` |

### 📊 **Sistem Komutları**
| Komut | Açıklama | İzin |
|-------|-----------|------|
| `/control logs <list/clear/clear-all>` | Log dosyalarını yönet | `antiescape.general` |
| `/control discord test` | Discord webhook'unu test et | `antiescape.general` |
| `/control reload` | Yapılandırmayı yeniden yükle | `antiescape.general` |
| `/control version` | Eklenti bilgilerini göster | `antiescape.general` |

---

## 🔐 **İzinler**

### 📋 **İzin Düğümleri**
```yaml
antiescape.general          # Tüm temel komutlara erişim
antiescape.chat            # Kontrol sohbetine erişim
antiescape.update.notify   # Güncelleme bildirimlerini al
```

---

## 📁 **Dosya Yapısı**

```
AntiEscape/
├── config.yml              # Ana yapılandırma
├── webhook.yml             # Discord webhook ayarları
├── lang/
│   ├── en.yml             # İngilizce dil dosyası
│   └── tr.yml             # Türkçe dil dosyası
├── logs/                   # Log dosyaları dizini
│   ├── antiescape-moves.log
│   ├── antiescape-commands.log
│   ├── antiescape-chat.log
│   ├── antiescape-damage.log
│   ├── antiescape-items.log
│   ├── antiescape-control.log
│   └── antiescape-general.log
└── data/                   # Veri dosyaları
    ├── control-history.yml # Kontrol geçmişi
    ├── notes.yml          # Oyuncu notları
    └── whitelist.yml      # Güvenlik beyaz listesi
```

---

## 🔧 **Yapılandırma**

### 📝 **Ana Yapılandırma (config.yml)**
```yaml
# Dil ve Debug
language: "tr"
debug: false

# Loglama Sistemi
logging:
  enabled: true
  log-moves: true
  log-commands: true
  log-chat: true
  log-damage: true
  log-items: true
  log-control: true
  log-general: true
  rotation:
    enabled: true
    max-size: "10MB"
    max-files: 5
    compress: true

# Gelişmiş Kontrol Sistemi
advanced-control:
  enabled: true
  time-limits:
    enabled: true
    max-duration: 3600
    warning-time: 300
    auto-release: true
  history:
    enabled: true
    max-entries: 1000
  notes:
    enabled: true
    max-notes: 10
    note-length: 200
  notifications:
    enabled: true
    action-bar: true
    title: true
    sound: true
    boss-bar: true

# Gelişmiş Güvenlik Sistemi
advanced-security:
  enabled: true
  ip-control:
    enabled: true
    max-accounts-per-ip: 3
  vpn-detection:
    enabled: false
    block-vpn: false
  suspicious-activity:
    enabled: false
    threshold: 5
    time-window: 10
  auto-ban:
    enabled: true
    escape-attempts: 3
    suspicious-activity: 5
    vpn-usage: 1
    escalation:
      enabled: true
      multiplier: 2
      max-duration: "1m"
```

### 🔗 **Discord Webhook (webhook.yml)**
```yaml
discord:
  enabled: true
  webhook-url: "WEBHOOK_URL_BURAYA"
  bot-name: "AntiEscape Bot"
  bot-avatar: ""

embeds:
  control:
    start:
      title: "🔒 Kontrol Başladı"
      description: "**%target%** **%controller%** tarafından kontrol altına alındı"
      color: "#FF6B6B"
      thumbnail: "%target_avatar%"
    end:
      title: "✅ Serbest Bırakıldı"
      description: "**%target%** **%controller%** tarafından serbest bırakıldı"
      color: "#51CF66"
    escape:
      title: "🚨 Kaçış Denemesi"
      description: "**%target%** kontrol sırasında kaçmaya çalıştı"
      color: "#FFA500"
  movement:
    title: "🚶 Hareket Denemesi"
    description: "**%player%** kontrol altındayken hareket etmeye çalıştı"
    color: "#FF6B6B"
```

---

## 🌍 **Dil Desteği**

### 🇹🇷 **Türkçe (tr.yml)**
```yaml
no-permission: "&cBu komutu kullanma yetkiniz yok!"
control-started: "&a%player% artık kontrol altında!"
control-finished-clean: "&a%player% kontrolden çıkarıldı!"
control-finished-ban: "&c%player% banlandı!"
```

### 🇺🇸 **English (en.yml)**
```yaml
no-permission: "&cYou don't have permission to use this command!"
control-started: "&a%player% is now under control!"
control-finished-clean: "&a%player% has been released from control!"
control-finished-ban: "&c%player% has been banned!"
```

---

## 📊 **Loglama Sistemi**

### 📝 **Log Türleri**
- **Hareketler**: Oyuncu hareket denemeleri
- **Komutlar**: Komut yürütme denemeleri
- **Sohbet**: Sohbet mesajları
- **Hasar**: Hasar olayları
- **Eşyalar**: Eşya düşürme denemeleri
- **Kontrol**: Kontrol başlangıç/bitiş olayları
- **Genel**: Genel eklenti olayları

### 🔄 **Log Rotasyonu**
- **Maksimum Boyut**: Log dosyası başına 10MB
- **Maksimum Dosya**: 5 yedek dosya
- **Sıkıştırma**: Eski logların otomatik sıkıştırılması

---

## 🛡️ **Güvenlik Özellikleri**

### 🔍 **IP Kontrolü**
- IP başına birden fazla hesabı izle
- Ülke bazlı kısıtlamalar
- Otomatik şüpheli aktivite tespiti

### 🚫 **VPN Tespiti**
- Gerçek zamanlı VPN/Proxy tespiti
- Yapılandırılabilir engelleme politikaları
- Güvenilir VPN'ler için beyaz liste

### ⚠️ **Şüpheli Aktivite**
- Hızlı hareket tespiti
- Komut spam önleme
- Sohbet taşkını koruması
- Otomatik eylem loglaması

### 🚨 **Otomatik Ban Sistemi**
- **Normal Banlar**: Güvenlik ihlalleri için
- **Kaçış Banları**: Oyuncular kontroldeyken çıktığında
- **Hareket Banları**: Hareket ihlalleri için (yapılandırılabilir)
- **Ban Artırımı**: Tekrarlayan ihlaller için süreyi artır

---

## 🔗 **Discord Entegrasyonu**

### 📢 **Webhook Olayları**
- **Kontrol Olayları**: Başlangıç, bitiş, kaçış denemeleri
- **Oyuncu Eylemleri**: Hareketler, komutlar, sohbet
- **Güvenlik Olayları**: VPN tespiti, şüpheli aktivite
- **Sistem Olayları**: Eklenti güncellemeleri, hatalar

### 🎨 **Özelleştirilebilir Embed'ler**
- Özel başlıklar ve açıklamalar
- Yapılandırılabilir renkler ve küçük resimler
- Dinamik alan oluşturma
- Alt bilgi özelleştirme

---

## 📈 **Performans**

### ⚡ **Optimizasyonlar**
- Asenkron webhook teslimi
- Verimli veri yapıları
- Yapılandırılabilir log seviyeleri
- Bellek verimli önbellekleme

### 📊 **İzleme**
- Gerçek zamanlı performans metrikleri
- Bellek kullanım takibi
- Olay işleme istatistikleri
- bStats entegrasyonu

---

## 🚀 **Gelişmiş Özellikler**

### 🎯 **Kontrol Oturumları**
- UUID'ler ile oturum takibi
- Süre izleme
- Kaçış denemesi kaydı
- Otomatik temizlik

### 📝 **Oyuncu Notları**
- Kalıcı not depolama
- Zaman damgası takibi
- Not uzunluğu limitleri
- Kolay yönetim komutları

### 📊 **İstatistik Sistemi**
- Kontrol süresi takibi
- Kaçış denemesi sayımı
- Şüpheli aktivite izleme
- IP adresi takibi

### 🔄 **Tab Tamamlama**
- Akıllı komut önerileri
- Oyuncu adı otomatik tamamlama
- Bağlama duyarlı tamamlamalar
- ArrayIndexOutOfBoundsException yok

---

## 🔧 **Geliştirme**

### 🛠️ **Kaynak Koddan Derleme**
```bash
# Depoyu klonla
git clone https://github.com/xOrcun/AntiEscape.git

# Dizine git
cd AntiEscape

# Maven ile derle
mvn clean package
```

### 📦 **Bağımlılıklar**
- **Bukkit/Spigot API**: Ana Minecraft sunucu API'si (1.16.5+)
- **Gson**: JSON işleme
- **bStats**: Anonim kullanım istatistikleri
- **Vault**: İzin sistemi entegrasyonu

### 🧪 **Test Etme**
- Spigot 1.16+ üzerinde test edildi
- Paper sunucuları ile uyumlu
- Bukkit uyumluluğu doğrulandı
- Büyük sunucularda performans test edildi

---

## 📋 **Değişiklik Geçmişi**

### 🆕 **Sürüm 1.3-Beta**
- **Gelişmiş Kontrol Sistemi**: Oturum yönetimi, geçmiş takibi, oyuncu notları
- **Gelişmiş Güvenlik Sistemi**: IP kontrolü, VPN tespiti, şüpheli aktivite izleme
- **Kapsamlı Loglama**: Her olay türü için ayrı log dosyaları
- **Discord Entegrasyonu**: Zengin webhook sistemi ve özelleştirilebilir embed'ler
- **Çok Dilli Destek**: İngilizce ve Türkçe ile otomatik oluşturma
- **Tab Tamamlama**: Akıllı komut önerileri ve oyuncu otomatik tamamlama
- **Otomatik Ban Sistemi**: Yapılandırılabilir ban süreleri ve artırım
- **Performans İyileştirmeleri**: Asenkron işlemler ve verimli veri yapıları

### 🔄 **Sürüm 1.2.0**
- Gelişmiş kontrol sistemi
- Temel loglama uygulaması
- Discord webhook desteği
- Çok dilli çerçeve

### 🆕 **Sürüm 1.1.0**
- Ana kontrol sistemi
- Hareket önleme
- Komut engelleme
- Sohbet izolasyonu

---

## 🤝 **Katkıda Bulunma**

Katkılarınızı bekliyoruz! Lütfen sorunları, özellik isteklerini veya pull request'leri göndermekten çekinmeyin.

### 📝 **Nasıl Katkıda Bulunulur**
1. Depoyu fork edin
2. Bir özellik dalı oluşturun
3. Değişikliklerinizi yapın
4. Pull request gönderin

### 🐛 **Sorun Bildirme**
- GitHub sorun takipçisini kullanın
- Detaylı bilgi sağlayın
- Mümkünse sunucu loglarını ekleyin
- Minecraft sürümünü belirtin

---

## 📄 **Lisans**

Bu proje **MIT Lisansı** altında lisanslanmıştır - detaylar için [LICENSE](LICENSE) dosyasına bakın.

---

## 🙏 **Teşekkürler**

- **Spigot Topluluğu** mükemmel Bukkit API'si için
- **bStats Ekibi** anonim kullanım istatistikleri için
- Bu eklentiyi geliştirmeye yardım eden **Tüm Katkıda Bulunanlar**

---

## 📞 **Destek**

### 🔗 **Bağlantılar**
- **GitHub**: [https://github.com/xOrcun/AntiEscape](https://github.com/xOrcun/AntiEscape)
- **Discord**: [https://orcunozturk.com/discord](https://orcunozturk.com/discord)
- **Website**: [https://orcunozturk.com](https://orcunozturk.com)

### 💬 **Yardım Alma**
- **Discord Sunucusu**: Destek için topluluğumuza katılın
- **GitHub Sorunları**: Hataları bildirin ve özellik isteyin
- **Dokümantasyon**: Yaygın çözümler için bu README'yi kontrol edin

---

<div align="center">

**❤️ ile [Orcun Ozturk](https://github.com/orcunozturk) tarafından yapıldı**

[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/xOrcun)
[![Discord](https://img.shields.io/badge/Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)](https://orcunozturk.com/discord)
[![Website](https://img.shields.io/badge/Website-FF6B6B?style=for-the-badge&logo=website&logoColor=white)](https://orcunozturk.com)

</div> 