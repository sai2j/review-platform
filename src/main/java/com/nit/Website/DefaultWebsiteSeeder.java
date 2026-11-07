package com.nit.Website;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Adds a starter list of well-known websites for every category, so that
 * category / search pages are never empty on a fresh database.
 *
 * - Runs at application start-up.
 * - Safe to run many times: a website whose domain already exists is not
 *   added again. If that existing website has NO category yet, the category
 *   (and country) from this list is filled in. A category that is already set
 *   is never changed.
 * - These websites start with 0 reviews and no rating. Real ratings and
 *   reviews only come from real users.
 * - Turn off with:  app.seed-websites=false
 */
@Component
public class DefaultWebsiteSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultWebsiteSeeder.class);

    private final WebsiteService websiteService;
    private final WebsiteRepository websiteRepository;
    private final boolean enabled;

    public DefaultWebsiteSeeder(
            WebsiteService websiteService,
            WebsiteRepository websiteRepository,
            @Value("${app.seed-websites:true}") boolean enabled) {
        this.websiteService = websiteService;
        this.websiteRepository = websiteRepository;
        this.enabled = enabled;
    }

    /** name, url, category, description */
    private static final List<String[]> SITES = List.of(
            // ---------- Bank (Finance & Insurance) ----------
            new String[] { "HDFC Bank", "https://www.hdfcbank.com", "Bank", "Private sector bank offering accounts, cards and loans." },
            new String[] { "State Bank of India", "https://sbi.co.in", "Bank", "Public sector bank with banking, loans and insurance services." },
            new String[] { "ICICI Bank", "https://www.icicibank.com", "Bank", "Bank offering savings accounts, credit cards and loans." },
            new String[] { "Axis Bank", "https://www.axisbank.com", "Bank", "Bank offering personal and business banking services." },
            new String[] { "Kotak Mahindra Bank", "https://www.kotak.com", "Bank", "Bank offering accounts, investments and loans." },
            new String[] { "Punjab National Bank", "https://www.pnbindia.in", "Bank", "Public sector bank with retail and corporate banking." },
            new String[] { "Bank of Baroda", "https://www.bankofbaroda.in", "Bank", "Public sector bank with retail and corporate banking." },
            new String[] { "Canara Bank", "https://canarabank.com", "Bank", "Public sector bank offering accounts and loans." },
            new String[] { "Union Bank of India", "https://www.unionbankofindia.co.in", "Bank", "Public sector bank with banking and loan services." },
            new String[] { "IDFC FIRST Bank", "https://www.idfcfirstbank.com", "Bank", "Bank offering savings accounts, cards and loans." },
            new String[] { "Yes Bank", "https://www.yesbank.in", "Bank", "Bank offering personal and business banking." },
            new String[] { "IndusInd Bank", "https://www.indusind.com", "Bank", "Bank offering accounts, cards and loans." },
            new String[] { "Bank of India", "https://bankofindia.co.in", "Bank", "Public sector bank with retail and corporate banking." },
            new String[] { "LIC of India", "https://licindia.in", "Bank", "Life insurance plans and services." },
            new String[] { "Bajaj Finserv", "https://www.bajajfinserv.in", "Bank", "Loans, insurance and investment services." },

            // ---------- Shopping ----------
            new String[] { "Amazon India", "https://www.amazon.in", "Shopping", "Online marketplace for electronics, fashion, home and more." },
            new String[] { "Flipkart", "https://www.flipkart.com", "Shopping", "Online marketplace for electronics, fashion and daily needs." },
            new String[] { "Myntra", "https://www.myntra.com", "Shopping", "Online store for fashion and lifestyle products." },
            new String[] { "Ajio", "https://www.ajio.com", "Shopping", "Online fashion and lifestyle store." },
            new String[] { "Meesho", "https://www.meesho.com", "Shopping", "Online marketplace for affordable fashion and home products." },
            new String[] { "Nykaa", "https://www.nykaa.com", "Shopping", "Online store for beauty, wellness and fashion." },
            new String[] { "Snapdeal", "https://www.snapdeal.com", "Shopping", "Online marketplace for fashion, electronics and home." },
            new String[] { "Tata CLiQ", "https://www.tatacliq.com", "Shopping", "Online store for fashion, electronics and lifestyle." },
            new String[] { "FirstCry", "https://www.firstcry.com", "Shopping", "Online store for baby and kids products." },
            new String[] { "Lenskart", "https://www.lenskart.com", "Shopping", "Online store for eyeglasses, sunglasses and contact lenses." },
            new String[] { "Purplle", "https://www.purplle.com", "Shopping", "Online store for beauty and personal care." },
            new String[] { "Decathlon India", "https://www.decathlon.in", "Shopping", "Sports and outdoor equipment store." },
            new String[] { "Pepperfry", "https://www.pepperfry.com", "Shopping", "Online store for furniture and home decor." },
            new String[] { "Urban Ladder", "https://www.urbanladder.com", "Shopping", "Online store for furniture and home decor." },
            new String[] { "JioMart", "https://www.jiomart.com", "Shopping", "Online grocery and shopping platform." },

            // ---------- Travel ----------
            new String[] { "MakeMyTrip", "https://www.makemytrip.com", "Travel", "Online booking for flights, hotels and holiday packages." },
            new String[] { "Goibibo", "https://www.goibibo.com", "Travel", "Online booking for flights, hotels and buses." },
            new String[] { "IRCTC", "https://www.irctc.co.in", "Travel", "Indian Railways online ticket booking." },
            new String[] { "Cleartrip", "https://www.cleartrip.com", "Travel", "Online booking for flights and hotels." },
            new String[] { "Booking.com", "https://www.booking.com", "Travel", "Online hotel and accommodation booking." },
            new String[] { "Airbnb", "https://www.airbnb.com", "Travel", "Marketplace for stays and experiences." },
            new String[] { "Yatra", "https://www.yatra.com", "Travel", "Online booking for flights, hotels and holidays." },
            new String[] { "EaseMyTrip", "https://www.easemytrip.com", "Travel", "Online booking for flights, hotels and holidays." },
            new String[] { "ixigo", "https://www.ixigo.com", "Travel", "Flight, train and bus booking app." },
            new String[] { "redBus", "https://www.redbus.in", "Travel", "Online bus ticket booking." },
            new String[] { "Agoda", "https://www.agoda.com", "Travel", "Online hotel and flight booking." },
            new String[] { "Expedia", "https://www.expedia.com", "Travel", "Online travel booking for flights, hotels and cars." },
            new String[] { "Tripadvisor", "https://www.tripadvisor.in", "Travel", "Travel guides, hotel and restaurant reviews." },
            new String[] { "OYO", "https://www.oyorooms.com", "Travel", "Hotel and stay booking platform." },
            new String[] { "IndiGo", "https://www.goindigo.in", "Travel", "Airline with flight booking and travel services." },

            // ---------- Food ----------
            new String[] { "Zomato", "https://www.zomato.com", "Food", "Food delivery and restaurant discovery." },
            new String[] { "Swiggy", "https://www.swiggy.com", "Food", "Food and grocery delivery." },
            new String[] { "Domino's India", "https://www.dominos.co.in", "Food", "Pizza ordering and delivery." },
            new String[] { "McDonald's India", "https://www.mcdonaldsindia.com", "Food", "Fast food restaurants and ordering." },
            new String[] { "Starbucks India", "https://www.starbucks.in", "Food", "Coffee and cafe chain." },
            new String[] { "BigBasket", "https://www.bigbasket.com", "Food", "Online grocery shopping and delivery." },
            new String[] { "KFC India", "https://online.kfc.co.in", "Food", "Fried chicken restaurants and online ordering." },
            new String[] { "Pizza Hut India", "https://www.pizzahut.co.in", "Food", "Pizza restaurants and online ordering." },
            new String[] { "Burger King India", "https://www.burgerking.in", "Food", "Burger restaurants and online ordering." },
            new String[] { "Subway India", "https://www.subway.com", "Food", "Sandwich restaurants and online ordering." },
            new String[] { "Blinkit", "https://blinkit.com", "Food", "Quick grocery delivery." },
            new String[] { "Zepto", "https://www.zeptonow.com", "Food", "Quick grocery delivery." },
            new String[] { "Cafe Coffee Day", "https://www.cafecoffeeday.com", "Food", "Coffee and cafe chain." },
            new String[] { "Barbeque Nation", "https://www.barbequenation.com", "Food", "Restaurant chain known for live grills." },
            new String[] { "FreshMenu", "https://www.freshmenu.com", "Food", "Online food ordering and delivery." },

            // ---------- Technology (Electronics & Technology) ----------
            new String[] { "Google", "https://www.google.com", "Technology", "Search engine and online services." },
            new String[] { "Apple", "https://www.apple.com", "Technology", "Consumer electronics, software and services." },
            new String[] { "Samsung", "https://www.samsung.com", "Technology", "Mobiles, TVs, appliances and electronics." },
            new String[] { "Dell", "https://www.dell.com", "Technology", "Laptops, desktops and computer accessories." },
            new String[] { "HP", "https://www.hp.com", "Technology", "Laptops, printers and computer accessories." },
            new String[] { "Croma", "https://www.croma.com", "Technology", "Electronics and appliances retailer." },
            new String[] { "Reliance Digital", "https://www.reliancedigital.in", "Technology", "Electronics and appliances retailer." },
            new String[] { "Microsoft", "https://www.microsoft.com", "Technology", "Software, devices and cloud services." },
            new String[] { "Lenovo", "https://www.lenovo.com", "Technology", "Laptops, desktops and tablets." },
            new String[] { "ASUS", "https://www.asus.com", "Technology", "Laptops, motherboards and electronics." },
            new String[] { "Acer", "https://www.acer.com", "Technology", "Laptops, monitors and computer accessories." },
            new String[] { "Sony", "https://www.sony.co.in", "Technology", "Televisions, audio, cameras and gaming." },
            new String[] { "LG", "https://www.lg.com", "Technology", "TVs, appliances and electronics." },
            new String[] { "OnePlus", "https://www.oneplus.in", "Technology", "Smartphones and accessories." },
            new String[] { "Xiaomi", "https://www.mi.com", "Technology", "Smartphones, TVs and smart devices." },
            new String[] { "boAt", "https://www.boat-lifestyle.com", "Technology", "Audio products and wearables." },

            // ---------- Education (Education & Training) ----------
            new String[] { "Udemy", "https://www.udemy.com", "Education", "Online courses taught by experts." },
            new String[] { "Coursera", "https://www.coursera.org", "Education", "Online courses and degrees from universities." },
            new String[] { "Khan Academy", "https://www.khanacademy.org", "Education", "Free lessons and practice for learners." },
            new String[] { "Unacademy", "https://unacademy.com", "Education", "Online learning for exam preparation." },
            new String[] { "BYJU'S", "https://byjus.com", "Education", "Online learning app for school students." },
            new String[] { "upGrad", "https://www.upgrad.com", "Education", "Online degrees and career courses." },
            new String[] { "edX", "https://www.edx.org", "Education", "Online courses from universities and institutions." },
            new String[] { "Skillshare", "https://www.skillshare.com", "Education", "Online classes for creative and practical skills." },
            new String[] { "LinkedIn Learning", "https://www.linkedin.com/learning", "Education", "Online courses for professional skills." },
            new String[] { "Physics Wallah", "https://www.pw.live", "Education", "Online learning for school and exam preparation." },
            new String[] { "Vedantu", "https://www.vedantu.com", "Education", "Live online classes for school students." },
            new String[] { "Simplilearn", "https://www.simplilearn.com", "Education", "Online certification and bootcamp courses." },
            new String[] { "Great Learning", "https://www.mygreatlearning.com", "Education", "Online courses and degree programs." },
            new String[] { "NPTEL", "https://nptel.ac.in", "Education", "Free online courses from IITs and IISc." },
            new String[] { "Codecademy", "https://www.codecademy.com", "Education", "Interactive coding lessons." });

    @Override
    public void run(String... args) {

        if (!enabled) {
            return;
        }

        int added = 0;

        for (String[] row : SITES) {
            try {
                Website website = new Website(row[0], row[1], row[3]);
                website.setCategory(row[2]);
                website.setCountry("India");

                /* saveWebsite returns the existing website when the domain is already there */
                Website saved = websiteService.saveWebsite(website);

                if (saved == website) {
                    added++;

                } else if (saved != null && isBlank(saved.getCategory())) {
                    /* website already existed without a category - fill it in */
                    saved.setCategory(row[2]);

                    if (isBlank(saved.getCountry())) {
                        saved.setCountry("India");
                    }

                    websiteRepository.save(saved);
                }
            } catch (Exception e) {
                log.warn("Could not add starter website {}: {}", row[0], e.getMessage());
            }
        }

        log.info("Starter websites checked: {} new, {} total in list", added, SITES.size());
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}