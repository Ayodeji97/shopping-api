package com.danzucker.shopping.catalog.data

import com.danzucker.shopping.catalog.domain.ProductDetails
import java.math.BigDecimal

/**
 * The catalog loaded at startup: 12 products across 6 categories and 11 brands, with prices
 * from 14.99 to 2,499.00, so every filter, search and pagination case has something to find.
 */
@Suppress("MagicNumber") // Seed data: the ids and prices are the data.
internal object SeedProducts {
    private fun image(slug: String) = "https://picsum.photos/seed/$slug/600/600"

    val all: List<ProductDetails> =
        listOf(
            ProductDetails(
                1,
                "AirPods Pro (2nd generation)",
                "Wireless earbuds with active noise cancellation, adaptive transparency and USB-C charging case.",
                image("airpods-pro"),
                BigDecimal("249.00"),
                "Apple",
                "Electronics",
            ),
            ProductDetails(
                2,
                "WH-1000XM5 Headphones",
                "Over-ear wireless headphones with industry-leading noise cancellation and 30-hour battery life.",
                image("wh-1000xm5"),
                BigDecimal("399.99"),
                "Sony",
                "Electronics",
            ),
            ProductDetails(
                3,
                "MacBook Pro 14\" M4",
                "14-inch laptop with the M4 chip, Liquid Retina XDR display and up to 24 hours of battery life.",
                image("macbook-pro"),
                BigDecimal("2499.00"),
                "Apple",
                "Electronics",
            ),
            ProductDetails(
                4,
                "Galaxy S25 Ultra",
                "Android smartphone with a 200MP camera, built-in S Pen and titanium frame.",
                image("galaxy-s25"),
                BigDecimal("1299.99"),
                "Samsung",
                "Electronics",
            ),
            ProductDetails(
                5,
                "Air Max 90",
                "Classic running-inspired sneakers with visible Air cushioning and a leather upper.",
                image("air-max-90"),
                BigDecimal("129.99"),
                "Nike",
                "Footwear",
            ),
            ProductDetails(
                6,
                "Ultraboost Light",
                "Lightweight running shoes with responsive Boost midsole for long-distance comfort.",
                image("ultraboost"),
                BigDecimal("189.95"),
                "Adidas",
                "Footwear",
            ),
            ProductDetails(
                7,
                "Dri-FIT Running Tee",
                "Breathable, sweat-wicking short-sleeve shirt for training and everyday runs.",
                image("dri-fit-tee"),
                BigDecimal("34.99"),
                "Nike",
                "Apparel",
            ),
            ProductDetails(
                8,
                "Classic Fit Denim Jacket",
                "Timeless trucker jacket in rigid cotton denim with button-flap chest pockets.",
                image("denim-jacket"),
                BigDecimal("89.50"),
                "Levi's",
                "Apparel",
            ),
            ProductDetails(
                9,
                "V15 Detect Cordless Vacuum",
                "Cordless stick vacuum with laser dust detection and up to 60 minutes of run time.",
                image("dyson-v15"),
                BigDecimal("749.99"),
                "Dyson",
                "Home",
            ),
            ProductDetails(
                10,
                "POÄNG Armchair",
                "Bentwood armchair with a layer-glued birch frame and washable cushion cover.",
                image("poang"),
                BigDecimal("129.00"),
                "IKEA",
                "Home",
            ),
            ProductDetails(
                11,
                "Hydrating Facial Cleanser",
                "Gentle, non-foaming cleanser with ceramides and hyaluronic acid for normal to dry skin.",
                image("cerave-cleanser"),
                BigDecimal("14.99"),
                "CeraVe",
                "Beauty",
            ),
            ProductDetails(
                12,
                "Kindle Paperwhite",
                "Waterproof e-reader with a 7-inch glare-free display, adjustable warm light and weeks of battery.",
                image("kindle"),
                BigDecimal("159.99"),
                "Amazon",
                "Books",
            ),
        )
}
