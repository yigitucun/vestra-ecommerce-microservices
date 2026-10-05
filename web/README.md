# 🌐 Vestra Web — Frontend Client

Vestra mikroservis ekosisteminin modern kullanıcı arayüzü ve yönetim paneli.

> ℹ️ Projenin genel mimarisi, mikroservisler, Kafka event akışları ve altyapı detayları için kök dizindeki [README.md](file:///C:/Users/Admin/Desktop/workspaces/vestra/README.md) dosyasına göz atabilirsiniz.

---

## 🛠️ Kullanılan Teknolojiler

- **Framework:** Next.js 16 (App Router) & React 19
- **Dil:** TypeScript 5
- **Stil & Tasarım:** Tailwind CSS v4, Lucide React Icons, Radix / Base UI, shadcn
- **State & Veri Çekme:** TanStack React Query v5
- **Tablolar:** TanStack React Table v9
- **Form & Validasyon:** React Hook Form & Zod v4
- **Görselleştirme & Grafikler:** Recharts v3
- **Etkileşim:** @dnd-kit (Sürükle-bırak desteği), Sonner (Toast bildirimleri), cmdk

---

## 🚀 Çalıştırma

```bash
# Bağımlılıkları yükleyin
npm install

# Geliştirme ortamını başlatın
npm run dev

# Tip kontrolü ve Lint
npm run typecheck
npm run lint

# Üretim derlemesi
npm run build
```

Uygulama varsayılan olarak **`http://localhost:3000`** adresinde çalışır ve API isteklerini Gateway (`http://localhost:8080/api`) üzerinden backend servislerine iletir.
