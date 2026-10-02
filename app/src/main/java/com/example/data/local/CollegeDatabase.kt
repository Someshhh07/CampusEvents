package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        EventEntity::class,
        CachedEventEntity::class,
        ClubEntity::class,
        ClubMemberEntity::class,
        EventHistoryEntity::class,
        EventReportEntity::class,
        NotificationEntity::class,
        ConflictEntity::class,
        EventApprovalDecisionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CollegeDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun cachedEventDao(): CachedEventDao
    abstract fun clubDao(): ClubDao
    abstract fun historyDao(): HistoryDao
    abstract fun reportDao(): ReportDao
    abstract fun notificationDao(): NotificationDao
    abstract fun conflictDao(): ConflictDao
    abstract fun approvalDao(): ApprovalDao

    companion object {
        @Volatile
        private var INSTANCE: CollegeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CollegeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CollegeDatabase::class.java,
                    "college_events_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }
        }

        suspend fun populateDatabase(database: CollegeDatabase) {
            val eventDao = database.eventDao()
            val cachedEventDao = database.cachedEventDao()
            val clubDao = database.clubDao()
            val historyDao = database.historyDao()
            val reportDao = database.reportDao()
            val notificationDao = database.notificationDao()
            val conflictDao = database.conflictDao()

            val now = System.currentTimeMillis()

            // 1. Seed Clubs
            val clubs = listOf(
                ClubEntity(
                    clubId = "club_coding",
                    name = "Coding Club",
                    tagline = "Innovate, Build, and Deploy the Future",
                    description = "The premier developer community on campus. Organizing hackathons, open source sprints, competitive programming challenges, and tech masterclasses.",
                    category = "Technical",
                    department = "Computer Science & Engineering",
                    leadPresident = "Rahul Verma",
                    leadPresidentId = "user_rahul",
                    memberCount = 142,
                    contactEmail = "codingclub@college.edu",
                    socialLink = "https://github.com/campus-coding-club",
                    isFollowed = true
                ),
                ClubEntity(
                    clubId = "club_cultural",
                    name = "Cultural Society",
                    tagline = "Celebrating Arts, Music, Drama & Campus Heritage",
                    description = "Bringing together musicians, dancers, actors, and artists to curate vibrant college festivals, acoustic nights, and dramatic productions.",
                    category = "Cultural",
                    department = "Campus Student Affairs",
                    leadPresident = "Ananya Sharma",
                    leadPresidentId = "user_ananya",
                    memberCount = 210,
                    contactEmail = "cultural@college.edu",
                    socialLink = "https://instagram.com/campusculture",
                    isFollowed = true
                ),
                ClubEntity(
                    clubId = "club_robotics",
                    name = "Robotics & Automation Club",
                    tagline = "Engineering Autonomous Systems & Aerial Bots",
                    description = "Hardware enthusiasts designing combat bots, quadcopters, industrial automation prototypes, and participating in national competitions.",
                    category = "Technical",
                    department = "Mechatronics & Mechanical Dept",
                    leadPresident = "Vikram Rao",
                    leadPresidentId = "user_vikram",
                    memberCount = 88,
                    contactEmail = "robotics@college.edu",
                    socialLink = "https://robotics.college.edu",
                    isFollowed = false
                ),
                ClubEntity(
                    clubId = "club_ieee",
                    name = "IEEE Student Branch",
                    tagline = "Advancing Technology for Humanity",
                    description = "Technical symposiums, peer research reviews, guest lectures from distinguished engineers, and international project collaborations.",
                    category = "Technical",
                    department = "Electronics & Communication",
                    leadPresident = "Karthik Menon",
                    leadPresidentId = "user_karthik",
                    memberCount = 95,
                    contactEmail = "ieee@college.edu",
                    socialLink = "https://ieee.college.edu",
                    isFollowed = false
                ),
                ClubEntity(
                    clubId = "club_literary",
                    name = "Literary & Debating Society",
                    tagline = "Where Words Resonate and Ideas Collide",
                    description = "Parliamentary debates, poetry slams, MUN conferences, creative writing retreats, and the annual college literary festival.",
                    category = "Cultural",
                    department = "Humanities & Social Sciences",
                    leadPresident = "Priya Sundaram",
                    leadPresidentId = "user_priya",
                    memberCount = 76,
                    contactEmail = "literary@college.edu",
                    socialLink = "https://litdeb.college.edu",
                    isFollowed = false
                ),
                ClubEntity(
                    clubId = "club_ai",
                    name = "AI & Machine Learning Guild",
                    tagline = "Empowering Students to Build Frontier Intelligence",
                    description = "Research paper reading groups, open-source model fine-tuning, hackathons, and GPU cluster access for cutting-edge generative AI.",
                    category = "Technical",
                    department = "Computer Science & Engineering",
                    leadPresident = "Siddharth Roy",
                    leadPresidentId = "user_siddharth",
                    memberCount = 168,
                    contactEmail = "aiguild@college.edu",
                    socialLink = "https://ai.college.edu",
                    isFollowed = true
                ),
                ClubEntity(
                    clubId = "club_cyber",
                    name = "CyberSec & Ethical Hacking Cell",
                    tagline = "Defending Cyberspace, Finding Zero-Days",
                    description = "Capture The Flag (CTF) training, reverse engineering workshops, vulnerability research, and national cybersecurity competitions.",
                    category = "Technical",
                    department = "Information Technology",
                    leadPresident = "Zoya Akhtar",
                    leadPresidentId = "user_zoya",
                    memberCount = 112,
                    contactEmail = "cybersec@college.edu",
                    socialLink = "https://cybersec.college.edu",
                    isFollowed = false
                ),
                ClubEntity(
                    clubId = "club_ecell",
                    name = "Entrepreneurship & Innovation Cell",
                    tagline = "From Dorm Room Ideas to Seed Funding",
                    description = "Incubating student ventures, hosting venture capitalist pitch decks, startup weekends, and mentorship from campus alumni founders.",
                    category = "Technical",
                    department = "School of Management & Business",
                    leadPresident = "Meera Krishnan",
                    leadPresidentId = "user_meera",
                    memberCount = 130,
                    contactEmail = "ecell@college.edu",
                    socialLink = "https://ecell.college.edu",
                    isFollowed = true
                ),
                ClubEntity(
                    clubId = "club_esports",
                    name = "Campus Esports & Gaming Clan",
                    tagline = "Competitive Gaming, Strategy & Tournaments",
                    description = "Organizing university leagues for Valorant, BGMI, Rocket League, and FIFA with pro-cast streaming and esports coaching.",
                    category = "Sports",
                    department = "Student Recreation & Wellness",
                    leadPresident = "Rohan Das",
                    leadPresidentId = "user_rohan",
                    memberCount = 240,
                    contactEmail = "esports@college.edu",
                    socialLink = "https://discord.gg/campus-esports",
                    isFollowed = false
                ),
                ClubEntity(
                    clubId = "club_photography",
                    name = "Aperture Photography & Film",
                    tagline = "Visual Storytelling, Cinema & Framing Campus Life",
                    description = "Darkroom techniques, portraiture walks, aerial drone videography, and covering all flagship campus celebrations.",
                    category = "Cultural",
                    department = "School of Arts & Media",
                    leadPresident = "Kabir Sen",
                    leadPresidentId = "user_kabir",
                    memberCount = 94,
                    contactEmail = "aperture@college.edu",
                    socialLink = "https://instagram.com/aperture_media",
                    isFollowed = false
                )
            )
            clubDao.insertClubs(clubs)

            // 2. Seed Club Members
            val members = listOf(
                ClubMemberEntity(
                    clubId = "club_coding",
                    userId = "user_rahul",
                    userName = "Rahul Verma",
                    userEmail = "rahul.v@college.edu",
                    roleInClub = "PRESIDENT",
                    status = "ACTIVE"
                ),
                ClubMemberEntity(
                    clubId = "club_coding",
                    userId = "user_neha",
                    userName = "Neha Gupta",
                    userEmail = "neha.g@college.edu",
                    roleInClub = "ADMIN",
                    status = "ACTIVE"
                ),
                ClubMemberEntity(
                    clubId = "club_coding",
                    userId = "user_arjun",
                    userName = "Arjun Patel",
                    userEmail = "arjun.p@college.edu",
                    roleInClub = "MEMBER",
                    status = "ACTIVE"
                ),
                ClubMemberEntity(
                    clubId = "club_coding",
                    userId = "user_divya",
                    userName = "Divya R.",
                    userEmail = "divya.r@college.edu",
                    roleInClub = "MEMBER",
                    status = "PENDING_APPROVAL"
                ),
                ClubMemberEntity(
                    clubId = "club_cultural",
                    userId = "user_ananya",
                    userName = "Ananya Sharma",
                    userEmail = "ananya.s@college.edu",
                    roleInClub = "PRESIDENT",
                    status = "ACTIVE"
                )
            )
            clubDao.insertMembers(members)

            // 3. Seed Events with full provenance & multi-source representation
            val events = listOf(
                EventEntity(
                    eventId = "EVT-2026-001",
                    title = "HackVerse 2026: 36-Hour National Hackathon",
                    description = "The flagship annual hackathon bringing together over 500 college innovators. Build solutions across AI/ML, Decentralized Web, HealthTech, and Smart Campus. Mentorship from leading engineers, ₹1,50,000 cash prize pool, and fast-track recruitment opportunities.",
                    category = "Technical",
                    subcategory = "Hackathon",
                    startDate = "2026-09-24",
                    endDate = "2026-09-26",
                    startTime = "09:00 AM",
                    endTime = "06:00 PM",
                    venue = "Main Tech Auditorium & Lab Complex C",
                    organizer = "Coding Club",
                    organizerType = "CLUB",
                    organizerId = "club_coding",
                    department = "Computer Science & Engineering",
                    odStatus = "AVAILABLE",
                    registrationRequired = true,
                    registrationUrl = "https://hackverse2026.college.edu/register",
                    contactInformation = "hackverse@college.edu | +91 98765 43210",
                    posterUrl = "poster_hackathon",
                    source = "Authorized College Event API v2",
                    sourceType = "COLLEGE_API",
                    sourceUrl = "https://api.college.edu/v2/events/EVT-2026-001",
                    externalId = "API-HACK-2026",
                    createdBy = "College API Sync Service",
                    updatedBy = "Club President (Rahul Verma)",
                    verifiedBy = "Dr. Ramanathan (Faculty Coordinator)",
                    verificationStatus = "VERIFIED",
                    eventStatus = "PUBLISHED",
                    createdAt = now - 86400000 * 5,
                    updatedAt = now - 3600000 * 4,
                    isSaved = true
                ),
                EventEntity(
                    eventId = "EVT-2026-002",
                    title = "Tarang 2026: Annual Inter-College Cultural Fest",
                    description = "Three days of mesmerizing dance battles, battle of the bands, classical musical tributes, theatrical drama, fashion show, and celebrity pro-nite concert. Food stalls, merchandise bazaars, and artistic photo installations across the quad.",
                    category = "Cultural",
                    subcategory = "Festival",
                    startDate = "2026-09-28",
                    endDate = "2026-09-30",
                    startTime = "04:30 PM",
                    endTime = "10:30 PM",
                    venue = "Open Air Amphitheatre & Central Lawns",
                    organizer = "Cultural Society",
                    organizerType = "CLUB",
                    organizerId = "club_cultural",
                    department = "Student Welfare Council",
                    odStatus = "AVAILABLE",
                    registrationRequired = false,
                    registrationUrl = "https://tarang2026.college.edu",
                    contactInformation = "cultural@college.edu | Prof. Meera Nair (Faculty Advisor)",
                    posterUrl = "poster_cultural",
                    source = "College Portal Feed",
                    sourceType = "COLLEGE_PORTAL",
                    sourceUrl = "https://portal.college.edu/notices/tarang2026",
                    externalId = "PORTAL-FEST-77",
                    createdBy = "Student Welfare Portal Admin",
                    updatedBy = "Club President (Ananya Sharma)",
                    verifiedBy = "Prof. Meera Nair (Faculty Coordinator)",
                    verificationStatus = "VERIFIED",
                    eventStatus = "PUBLISHED",
                    createdAt = now - 86400000 * 7,
                    updatedAt = now - 3600000 * 8,
                    isSaved = true
                ),
                EventEntity(
                    eventId = "EVT-2026-003",
                    title = "Autonomous Drone Navigation & Computer Vision Workshop",
                    description = "Hands-on masterclass on building visual SLAM algorithms, obstacle avoidance sensors, and micro-controller telemetry for UAVs. Hardware kits provided during workshop sessions. Attendance certified by IEEE Aerospace Chapter.",
                    category = "Technical",
                    subcategory = "Workshop",
                    startDate = "2026-09-21",
                    endDate = "2026-09-21",
                    startTime = "10:00 AM",
                    endTime = "04:00 PM",
                    venue = "Robotics Lab 3, Mechanical Block 2",
                    organizer = "Robotics Club",
                    organizerType = "CLUB",
                    organizerId = "club_robotics",
                    department = "Mechatronics & Robotics",
                    odStatus = "AVAILABLE",
                    registrationRequired = true,
                    registrationUrl = "https://forms.college.edu/droneworkshop",
                    contactInformation = "robotics@college.edu",
                    posterUrl = "poster_hackathon",
                    source = "Club President Submission",
                    sourceType = "CLUB_PRESIDENT",
                    createdBy = "Vikram Rao (Club President)",
                    updatedBy = "Vikram Rao",
                    verifiedBy = "Dr. A.K. Sen (HOD Mechanical)",
                    verificationStatus = "VERIFIED",
                    eventStatus = "PUBLISHED",
                    createdAt = now - 86400000 * 3,
                    updatedAt = now - 3600000 * 2,
                    isSaved = false
                ),
                EventEntity(
                    eventId = "EVT-2026-004",
                    title = "Web3 & Zero Knowledge Proofs Research Talk",
                    description = "Special guest lecture by visiting MIT researcher Dr. Elena Vance. Covering succinct cryptographic proofs, privacy-preserving state channels, and scaling L2 architectures.",
                    category = "Technical",
                    subcategory = "Seminar",
                    startDate = "2026-09-23",
                    endDate = "2026-09-23",
                    startTime = "11:00 AM",
                    endTime = "01:00 PM",
                    venue = "Seminar Hall B-204",
                    organizer = "IEEE Student Branch",
                    organizerType = "CLUB",
                    organizerId = "club_ieee",
                    department = "Computer Science & Engineering",
                    odStatus = "AVAILABLE",
                    registrationRequired = true,
                    registrationUrl = "https://ieee.college.edu/zkp-talk",
                    contactInformation = "ieee@college.edu",
                    posterUrl = "",
                    source = "Club President Submission",
                    sourceType = "CLUB_PRESIDENT",
                    createdBy = "Karthik Menon",
                    updatedBy = "Karthik Menon",
                    verifiedBy = "",
                    verificationStatus = "CONFLICT",
                    eventStatus = "PUBLISHED",
                    createdAt = now - 86400000 * 2,
                    updatedAt = now - 3600000,
                    isSaved = false,
                    conflictDetails = "College API scheduled 10:00 AM at Hall A-101, whereas Club President submitted 11:00 AM at Hall B-204."
                ),
                EventEntity(
                    eventId = "EVT-2026-005",
                    title = "Campus Acoustic Music Night & Open Mic",
                    description = "An unplugged musical evening under the stars. Guitar duets, beatboxing, indie originals, and poetry performances by students and faculty members. Warm coffee and snacks served.",
                    category = "Cultural",
                    subcategory = "Music",
                    startDate = "2026-09-20",
                    endDate = "2026-09-20",
                    startTime = "06:30 PM",
                    endTime = "09:30 PM",
                    venue = "Student Activity Center Courtyard",
                    organizer = "Cultural Society",
                    organizerType = "CLUB",
                    organizerId = "club_cultural",
                    department = "Campus Student Affairs",
                    odStatus = "NOT_APPLICABLE",
                    registrationRequired = false,
                    registrationUrl = "",
                    contactInformation = "cultural@college.edu",
                    posterUrl = "poster_cultural",
                    source = "Club Admin",
                    sourceType = "CLUB_ADMIN",
                    createdBy = "Ananya Sharma",
                    updatedBy = "Ananya Sharma",
                    verifiedBy = "Prof. Meera Nair",
                    verificationStatus = "VERIFIED",
                    eventStatus = "PUBLISHED",
                    createdAt = now - 86400000 * 4,
                    updatedAt = now - 3600000 * 12,
                    isSaved = false
                ),
                EventEntity(
                    eventId = "EVT-2026-006",
                    title = "Quantum Computing Foundations & Qiskit Lab",
                    description = "Introductory hands-on session on quantum gates, superposition, entanglement, and executing circuits on IBM Quantum hardware via Qiskit. Extracted from faculty poster notice.",
                    category = "Technical",
                    subcategory = "Workshop",
                    startDate = "2026-09-25",
                    endDate = "2026-09-25",
                    startTime = "02:00 PM",
                    endTime = "05:00 PM",
                    venue = "Physics Advanced Computing Center",
                    organizer = "Physics & CSE Dept",
                    organizerType = "DEPARTMENT",
                    organizerId = "dept_cse",
                    department = "Physics & Computer Science",
                    odStatus = "PENDING",
                    registrationRequired = true,
                    registrationUrl = "https://quantum.college.edu",
                    contactInformation = "qc-lab@college.edu",
                    posterUrl = "",
                    source = "AI Extraction (Poster/Notice)",
                    sourceType = "AI_EXTRACTION",
                    createdBy = "AI Extraction Engine v1.0",
                    updatedBy = "AI Assistant",
                    verifiedBy = "",
                    verificationStatus = "PENDING",
                    eventStatus = "DRAFT",
                    createdAt = now - 3600000 * 5,
                    updatedAt = now - 3600000 * 5,
                    isSaved = false
                ),
                EventEntity(
                    eventId = "EVT-2026-007",
                    title = "Inter-Department Football Super Cup 2026",
                    description = "16 departments battle it out for the collegiate championship trophy. Matches under floodlights with official university referees and live commentary.",
                    category = "Sports",
                    subcategory = "Tournament",
                    startDate = "2026-09-27",
                    endDate = "2026-10-02",
                    startTime = "05:00 PM",
                    endTime = "08:00 PM",
                    venue = "Main University Sports Stadium",
                    organizer = "Sports Council",
                    organizerType = "COLLEGE",
                    organizerId = "council_sports",
                    department = "Physical Education Dept",
                    odStatus = "AVAILABLE",
                    registrationRequired = true,
                    registrationUrl = "https://sports.college.edu/football2026",
                    contactInformation = "sports@college.edu | Coach R. K. Singh",
                    posterUrl = "",
                    source = "Faculty Notice",
                    sourceType = "FACULTY",
                    createdBy = "Dr. D. S. Rawat (Director of Sports)",
                    updatedBy = "Dr. D. S. Rawat",
                    verifiedBy = "Dr. D. S. Rawat",
                    verificationStatus = "VERIFIED",
                    eventStatus = "PUBLISHED",
                    createdAt = now - 86400000 * 6,
                    updatedAt = now - 86400000,
                    isSaved = false
                ),
                EventEntity(
                    eventId = "EVT-2026-008",
                    title = "Full-Stack Cloud Native Development Sprint",
                    description = "Student-submitted hands-on boot camp covering Docker, Kubernetes, gRPC microservices, and CI/CD pipelines. Submitted by member Arjun Patel for club approval.",
                    category = "Technical",
                    subcategory = "Bootcamp",
                    startDate = "2026-10-05",
                    endDate = "2026-10-06",
                    startTime = "09:30 AM",
                    endTime = "04:30 PM",
                    venue = "Computing Lab 4, CSE Wing",
                    organizer = "Coding Club",
                    organizerType = "CLUB",
                    organizerId = "club_coding",
                    department = "Computer Science & Engineering",
                    odStatus = "PENDING",
                    registrationRequired = true,
                    registrationUrl = "",
                    contactInformation = "arjun.p@college.edu",
                    posterUrl = "",
                    source = "Club Member Submission",
                    sourceType = "CLUB_MEMBER",
                    createdBy = "Arjun Patel (Club Member)",
                    updatedBy = "Arjun Patel",
                    verifiedBy = "",
                    verificationStatus = "PENDING",
                    eventStatus = "PENDING",
                    createdAt = now - 3600000 * 10,
                    updatedAt = now - 3600000 * 10,
                    isSaved = false
                ),
                EventEntity(
                    eventId = "EVT-2026-009",
                    title = "HackVerse 2026 - College API Mirror",
                    description = "National collegiate hackathon - mirror entry imported from API. Similar to EVT-2026-001.",
                    category = "Technical",
                    subcategory = "Hackathon",
                    startDate = "2026-09-24",
                    endDate = "2026-09-26",
                    startTime = "09:00 AM",
                    endTime = "06:00 PM",
                    venue = "Main Tech Auditorium",
                    organizer = "Coding Club",
                    organizerType = "CLUB",
                    organizerId = "club_coding",
                    department = "Computer Science & Engineering",
                    odStatus = "AVAILABLE",
                    registrationRequired = true,
                    registrationUrl = "https://hackverse2026.college.edu",
                    contactInformation = "api-sync@college.edu",
                    posterUrl = "",
                    source = "College Official API",
                    sourceType = "COLLEGE_API",
                    createdBy = "API Import Worker",
                    updatedBy = "API Import Worker",
                    verifiedBy = "",
                    verificationStatus = "PENDING",
                    eventStatus = "PENDING",
                    createdAt = now - 3600000 * 2,
                    updatedAt = now - 3600000 * 2,
                    isSaved = false,
                    isDuplicateCandidate = true,
                    duplicateWithId = "EVT-2026-001"
                ),
                EventEntity(
                    eventId = "EVT-2026-010",
                    title = "Generative AI Agents & Frontier LLMs Workshop",
                    description = "Comprehensive masterclass on agentic workflows, LangChain, tool calling, and deploying quantized local models using Ollama. High-performance GPU cloud credits provided to every attendee.",
                    category = "Technical",
                    subcategory = "Workshop",
                    startDate = "2026-09-22",
                    endDate = "2026-09-22",
                    startTime = "02:00 PM",
                    endTime = "05:30 PM",
                    venue = "Turing Auditorium (AB1-201)",
                    organizer = "AI & Machine Learning Guild",
                    organizerType = "CLUB",
                    organizerId = "club_ai",
                    department = "Computer Science & Engineering",
                    odStatus = "AVAILABLE",
                    registrationRequired = true,
                    registrationUrl = "https://ai.college.edu/agents-workshop",
                    contactInformation = "aiguild@college.edu | Siddharth Roy (+91 91234 56780)",
                    posterUrl = "poster_hackathon",
                    source = "via AI Guild Portal",
                    sourceType = "CLUB_PRESIDENT",
                    createdBy = "Siddharth Roy (AI Guild Lead)",
                    updatedBy = "Siddharth Roy",
                    verifiedBy = "Dr. Ramanathan (Faculty Coordinator)",
                    verificationStatus = "VERIFIED",
                    eventStatus = "PUBLISHED",
                    createdAt = now - 86400000 * 2,
                    updatedAt = now - 3600000 * 3,
                    isSaved = true
                ),
                EventEntity(
                    eventId = "EVT-2026-011",
                    title = "CyberShield CTF: 24-Hr Capture The Flag",
                    description = "National cybersecurity competition featuring challenges across binary exploitation, web vulnerabilities, cryptography, steganography, and reverse engineering. Cash awards and industry recruiter interviews.",
                    category = "Technical",
                    subcategory = "Competition",
                    startDate = "2026-09-25",
                    endDate = "2026-09-26",
                    startTime = "10:00 AM",
                    endTime = "10:00 AM",
                    venue = "Networking Lab 2 (AB3-305)",
                    organizer = "CyberSec & Ethical Hacking Cell",
                    organizerType = "CLUB",
                    organizerId = "club_cyber",
                    department = "Information Technology",
                    odStatus = "AVAILABLE",
                    registrationRequired = true,
                    registrationUrl = "https://cybershield.college.edu",
                    contactInformation = "cybersec@college.edu",
                    posterUrl = "poster_hackathon",
                    source = "via CyberSec Cell",
                    sourceType = "CLUB_PRESIDENT",
                    createdBy = "Zoya Akhtar",
                    updatedBy = "Zoya Akhtar",
                    verifiedBy = "Prof. S. K. Mitra",
                    verificationStatus = "VERIFIED",
                    eventStatus = "PUBLISHED",
                    createdAt = now - 86400000 * 3,
                    updatedAt = now - 3600000 * 4,
                    isSaved = false
                ),
                EventEntity(
                    eventId = "EVT-2026-012",
                    title = "Battle of the Bands & Rock Night Showcase",
                    description = "Live electrifying performances by 10 top university bands competing for the Golden Pick Trophy. High-voltage rock, blues, alternative, and progressive originals.",
                    category = "Cultural",
                    subcategory = "Music",
                    startDate = "2026-09-23",
                    endDate = "2026-09-23",
                    startTime = "06:00 PM",
                    endTime = "10:00 PM",
                    venue = "Open Air Amphitheatre",
                    organizer = "Cultural Society",
                    organizerType = "CLUB",
                    organizerId = "club_cultural",
                    department = "Campus Student Affairs",
                    odStatus = "PENDING",
                    registrationRequired = false,
                    registrationUrl = "",
                    contactInformation = "cultural@college.edu",
                    posterUrl = "poster_cultural",
                    source = "via Cultural Society",
                    sourceType = "CLUB_ADMIN",
                    createdBy = "Ananya Sharma",
                    updatedBy = "Ananya Sharma",
                    verifiedBy = "Prof. Meera Nair",
                    verificationStatus = "VERIFIED",
                    eventStatus = "PUBLISHED",
                    createdAt = now - 86400000 * 4,
                    updatedAt = now - 3600000 * 6,
                    isSaved = true
                ),
                EventEntity(
                    eventId = "EVT-2026-013",
                    title = "Campus Startup Pitch Deck & Venture Demo Day",
                    description = "Student founders pitch their MVP prototypes to angel investors, campus incubation mentors, and venture capital partners. ₹10 Lakhs seed grant fund up for grabs.",
                    category = "Technical",
                    subcategory = "Entrepreneurship",
                    startDate = "2026-09-29",
                    endDate = "2026-09-29",
                    startTime = "10:30 AM",
                    endTime = "04:30 PM",
                    venue = "Incubation Center Hall 1 (AB4-102)",
                    organizer = "Entrepreneurship & Innovation Cell",
                    organizerType = "CLUB",
                    organizerId = "club_ecell",
                    department = "School of Management & Business",
                    odStatus = "AVAILABLE",
                    registrationRequired = true,
                    registrationUrl = "https://ecell.college.edu/demoday",
                    contactInformation = "ecell@college.edu | Meera Krishnan",
                    posterUrl = "poster_hackathon",
                    source = "via E-Cell",
                    sourceType = "CLUB_PRESIDENT",
                    createdBy = "Meera Krishnan",
                    updatedBy = "Meera Krishnan",
                    verifiedBy = "Dr. Ramanathan",
                    verificationStatus = "VERIFIED",
                    eventStatus = "PUBLISHED",
                    createdAt = now - 86400000 * 5,
                    updatedAt = now - 3600000 * 7,
                    isSaved = false
                ),
                EventEntity(
                    eventId = "EVT-2026-014",
                    title = "Valorant & BGMI Campus Championship Finals",
                    description = "The ultimate collegiate esports clash broadcasted on 4K arena screens with live shoutcasters. Winners qualify for the All-India University Esports Series.",
                    category = "Sports",
                    subcategory = "Esports",
                    startDate = "2026-09-22",
                    endDate = "2026-09-22",
                    startTime = "04:00 PM",
                    endTime = "09:00 PM",
                    venue = "Student Activity Center Arena",
                    organizer = "Campus Esports & Gaming Clan",
                    organizerType = "CLUB",
                    organizerId = "club_esports",
                    department = "Student Recreation & Wellness",
                    odStatus = "AVAILABLE",
                    registrationRequired = true,
                    registrationUrl = "https://discord.gg/campus-esports",
                    contactInformation = "esports@college.edu | Rohan Das",
                    posterUrl = "poster_cultural",
                    source = "via Esports League",
                    sourceType = "CLUB_PRESIDENT",
                    createdBy = "Rohan Das",
                    updatedBy = "Rohan Das",
                    verifiedBy = "Dr. D. S. Rawat",
                    verificationStatus = "VERIFIED",
                    eventStatus = "PUBLISHED",
                    createdAt = now - 86400000 * 3,
                    updatedAt = now - 3600000 * 5,
                    isSaved = true
                ),
                EventEntity(
                    eventId = "EVT-2026-015",
                    title = "Visual Framing & Drone Cinema Masterclass",
                    description = "Interactive filmmaking clinic covering composition rules, color grading in DaVinci Resolve, and cinematic drone camera maneuvers on campus.",
                    category = "Cultural",
                    subcategory = "Photography",
                    startDate = "2026-09-26",
                    endDate = "2026-09-26",
                    startTime = "02:00 PM",
                    endTime = "05:00 PM",
                    venue = "Media Lab Screening Room (Arts Wing)",
                    organizer = "Aperture Photography & Film",
                    organizerType = "CLUB",
                    organizerId = "club_photography",
                    department = "School of Arts & Media",
                    odStatus = "NOT_APPLICABLE",
                    registrationRequired = true,
                    registrationUrl = "https://instagram.com/aperture_media",
                    contactInformation = "aperture@college.edu",
                    posterUrl = "poster_cultural",
                    source = "via Photography Club",
                    sourceType = "CLUB_PRESIDENT",
                    createdBy = "Kabir Sen",
                    updatedBy = "Kabir Sen",
                    verifiedBy = "Prof. Meera Nair",
                    verificationStatus = "VERIFIED",
                    eventStatus = "PUBLISHED",
                    createdAt = now - 86400000 * 2,
                    updatedAt = now - 3600000 * 2,
                    isSaved = false
                )
            )
            eventDao.insertEvents(events)
            val cachedEntities = events.map {
                CachedEventEntity(
                    eventId = it.eventId,
                    title = it.title,
                    description = it.description,
                    category = it.category,
                    subcategory = it.subcategory,
                    startDate = it.startDate,
                    endDate = it.endDate,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    venue = it.venue,
                    organizer = it.organizer,
                    organizerType = it.organizerType,
                    organizerId = it.organizerId,
                    department = it.department,
                    odStatus = it.odStatus,
                    registrationRequired = it.registrationRequired,
                    registrationUrl = it.registrationUrl,
                    contactInformation = it.contactInformation,
                    posterUrl = it.posterUrl,
                    source = it.source,
                    sourceType = it.sourceType,
                    sourceUrl = it.sourceUrl,
                    externalId = it.externalId,
                    createdBy = it.createdBy,
                    updatedBy = it.updatedBy,
                    verifiedBy = it.verifiedBy,
                    verificationStatus = it.verificationStatus,
                    eventStatus = it.eventStatus,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    isSaved = it.isSaved,
                    conflictDetails = it.conflictDetails,
                    isDuplicateCandidate = it.isDuplicateCandidate,
                    duplicateWithId = it.duplicateWithId,
                    cachedAt = now,
                    isSyncedWithFirestore = true
                )
            }
            cachedEventDao.insertCachedEvents(cachedEntities)

            // 4. Seed Provenance History Records
            val histories = listOf(
                EventHistoryEntity(
                    eventId = "EVT-2026-001",
                    userId = "user_system",
                    userName = "College API Sync Engine",
                    userRole = "COLLEGE_API",
                    changeType = "CREATED",
                    oldValue = "None",
                    newValue = "Event record synchronized from Central API v2",
                    reason = "Scheduled 15-minute synchronization cycle",
                    timestamp = now - 86400000 * 5
                ),
                EventHistoryEntity(
                    eventId = "EVT-2026-001",
                    userId = "user_rahul",
                    userName = "Rahul Verma",
                    userRole = "CLUB_PRESIDENT",
                    changeType = "VENUE_CHANGED",
                    oldValue = "Tech Auditorium A1",
                    newValue = "Main Tech Auditorium & Lab Complex C",
                    reason = "Participant registrations exceeded 400 capacity",
                    timestamp = now - 86400000 * 3
                ),
                EventHistoryEntity(
                    eventId = "EVT-2026-001",
                    userId = "user_ramanathan",
                    userName = "Dr. Ramanathan",
                    userRole = "FACULTY",
                    changeType = "OD_UPDATED",
                    oldValue = "OD Pending",
                    newValue = "OD Available (Full 3 Days)",
                    reason = "Approved by Academic Dean for all CSE/IT/ECE participants",
                    timestamp = now - 86400000 * 2
                ),
                EventHistoryEntity(
                    eventId = "EVT-2026-001",
                    userId = "user_ramanathan",
                    userName = "Dr. Ramanathan",
                    userRole = "FACULTY",
                    changeType = "VERIFIED",
                    oldValue = "Pending Verification",
                    newValue = "Verified Official",
                    reason = "All sponsorship and venue permits cleared",
                    timestamp = now - 3600000 * 4
                ),
                EventHistoryEntity(
                    eventId = "EVT-2026-004",
                    userId = "user_system",
                    userName = "Conflict Detection Service",
                    userRole = "SYSTEM",
                    changeType = "CONFLICT_FLAGGED",
                    oldValue = "API Time: 10:00 AM",
                    newValue = "Club Time: 11:00 AM",
                    reason = "Mismatch detected between College Portal API schedule and Club President manual update",
                    timestamp = now - 3600000
                )
            )
            historyDao.insertHistories(histories)

            // 5. Seed Reports
            val reports = listOf(
                EventReportEntity(
                    reportId = "REP-101",
                    eventId = "EVT-2026-004",
                    eventTitle = "Web3 & Zero Knowledge Proofs Research Talk",
                    userId = "user_student_1",
                    userName = "Siddharth Rao (Student)",
                    issueType = "Wrong time",
                    description = "Department notice board shows 10:00 AM but the club announcement mentions 11:00 AM. Please clarify.",
                    status = "PENDING",
                    submittedAt = now - 3600000 * 3,
                    resolutionNotes = ""
                ),
                EventReportEntity(
                    reportId = "REP-102",
                    eventId = "EVT-2026-003",
                    eventTitle = "Autonomous Drone Navigation & Computer Vision Workshop",
                    userId = "user_student_2",
                    userName = "Megha Krishnan (Student)",
                    issueType = "Wrong venue",
                    description = "Lab 3 is undergoing renovation; club moved intro to Lecture Hall 1.",
                    status = "RESOLVED",
                    submittedAt = now - 86400000 * 2,
                    resolutionNotes = "Verified with Club President; updated venue to Robotics Lab 3 in Mechanical Block."
                )
            )
            for (report in reports) {
                reportDao.insertReport(report)
            }

            // 6. Seed Notifications
            val notifications = listOf(
                NotificationEntity(
                    title = "OD Status Approved: HackVerse 2026",
                    message = "Official OD (On-Duty) has been granted for all 3 days by Dr. Ramanathan.",
                    type = "OD_UPDATED",
                    eventId = "EVT-2026-001",
                    timestamp = now - 3600000 * 2,
                    isRead = false
                ),
                NotificationEntity(
                    title = "Tarang 2026 Registration Open",
                    message = "Cultural Society has released entry passes and event schedules.",
                    type = "CLUB_UPDATE",
                    eventId = "EVT-2026-002",
                    timestamp = now - 3600000 * 8,
                    isRead = false
                ),
                NotificationEntity(
                    title = "Venue Updated: Coding Club",
                    message = "HackVerse 2026 venue expanded to include Lab Complex C.",
                    type = "VENUE_CHANGED",
                    eventId = "EVT-2026-001",
                    timestamp = now - 86400000,
                    isRead = true
                )
            )
            notificationDao.insertNotifications(notifications)

            // 7. Seed Conflict
            val conflict = ConflictEntity(
                conflictId = "CONF-001",
                eventId = "EVT-2026-004",
                eventTitle = "Web3 & Zero Knowledge Proofs Research Talk",
                field = "Time & Venue",
                sourceA = "College Official API",
                valueA = "10:00 AM @ Hall A-101",
                sourceB = "Club President Submission",
                valueB = "11:00 AM @ Seminar Hall B-204",
                detectedAt = now - 3600000 * 2,
                isResolved = false
            )
            conflictDao.insertConflict(conflict)

            // 8. Seed Approval Decisions
            val approvalDao = database.approvalDao()
            val decisions = listOf(
                EventApprovalDecisionEntity(
                    eventId = "EVT-2026-001",
                    eventTitle = "HackVerse 2026: 36-Hour National Hackathon",
                    clubId = "club_coding",
                    reviewerId = "user_rahul",
                    reviewerName = "Rahul Verma",
                    reviewerRole = "PRESIDENT",
                    decision = "PUBLISHED",
                    previousStatus = "APPROVED",
                    newStatus = "PUBLISHED",
                    comments = "Official college budget and venue confirmed.",
                    timestamp = now - 86400000 * 3
                ),
                EventApprovalDecisionEntity(
                    eventId = "EVT-2026-002",
                    eventTitle = "Tandav: Annual Inter-College Cultural Carnival",
                    clubId = "club_cultural",
                    reviewerId = "user_ananya",
                    reviewerName = "Ananya Sharma",
                    reviewerRole = "PRESIDENT",
                    decision = "APPROVED",
                    previousStatus = "UNDER_REVIEW",
                    newStatus = "APPROVED",
                    comments = "Approved stage allocation and auditorium permission.",
                    timestamp = now - 86400000 * 2
                )
            )
            approvalDao.insertDecisions(decisions)
        }
    }
}
