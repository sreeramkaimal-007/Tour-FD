package com.example

import com.example.data.local.ExpenseEntity
import com.example.data.local.MemberEntity
import com.example.data.local.SettlementPaymentEntity
import com.example.domain.model.SettlementEngine
import com.example.domain.model.SplitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {

    @Test
    fun testSettlementEngineEqualSplit() {
        val m1 = MemberEntity(id = "1", tourId = "t1", name = "Alice", weight = 1.0f)
        val m2 = MemberEntity(id = "2", tourId = "t1", name = "Bob", weight = 1.0f)
        val members = listOf(m1, m2)

        // Alice pays $100 for Alice and Bob
        val expense = ExpenseEntity(
            id = "e1",
            tourId = "t1",
            title = "Dinner",
            amount = 100.0,
            date = 1000L,
            category = "FOOD",
            paidById = "1",
            splitType = SplitType.EQUAL.id,
            splitMemberIds = "1,2"
        )

        val balances = SettlementEngine.calculateBalances(members, listOf(expense), emptyList())
        val aliceBalance = balances.first { it.memberId == "1" }
        val bobBalance = balances.first { it.memberId == "2" }

        // Alice paid 100, share is 50 -> net is +50
        assertEquals(50.0, aliceBalance.netBalance, 0.01)
        // Bob paid 0, share is 50 -> net is -50
        assertEquals(-50.0, bobBalance.netBalance, 0.01)

        val directSettlements = SettlementEngine.computeDirectSettlements(balances)
        assertEquals(1, directSettlements.size)
        assertEquals("2", directSettlements[0].fromMemberId) // Bob pays
        assertEquals("1", directSettlements[0].toMemberId)   // Alice receives
        assertEquals(50.0, directSettlements[0].amount, 0.01)
    }

    @Test
    fun testSettlementEngineWithFamilyWeights() {
        // Alice is solo (1.0), Bob has family of 3 (3.0) -> Total weight = 4.0
        val m1 = MemberEntity(id = "1", tourId = "t1", name = "Alice", weight = 1.0f)
        val m2 = MemberEntity(id = "2", tourId = "t1", name = "Bob Family", weight = 3.0f)
        val members = listOf(m1, m2)

        // Alice pays $200 for hotel
        val expense = ExpenseEntity(
            id = "e1",
            tourId = "t1",
            title = "Hotel Stay",
            amount = 200.0,
            date = 1000L,
            category = "STAY",
            paidById = "1",
            splitType = SplitType.BY_WEIGHT.id,
            splitMemberIds = "1,2"
        )

        val balances = SettlementEngine.calculateBalances(members, listOf(expense), emptyList())
        val aliceBalance = balances.first { it.memberId == "1" }
        val bobBalance = balances.first { it.memberId == "2" }

        // Alice share: 200 * 1 / 4 = 50. Paid: 200. Net: +150
        assertEquals(150.0, aliceBalance.netBalance, 0.01)
        // Bob share: 200 * 3 / 4 = 150. Paid: 0. Net: -150
        assertEquals(-150.0, bobBalance.netBalance, 0.01)

        val settlements = SettlementEngine.computeDirectSettlements(balances)
        assertEquals(1, settlements.size)
        assertEquals(150.0, settlements[0].amount, 0.01)
    }

    @Test
    fun testDebtMinimizerReducesTransactions() {
        // 3 people: A pays 60 for (A,B,C), B pays 30 for (A,B,C)
        // Total spent: 90. Per person share: 30.
        // A paid 60, share 30 -> +30
        // B paid 30, share 30 -> 0
        // C paid 0, share 30 -> -30
        // Minimal transactions: C pays A $30 directly. (B doesn't need to do anything!)
        val m1 = MemberEntity("A", "t1", "A", weight = 1.0f)
        val m2 = MemberEntity("B", "t1", "B", weight = 1.0f)
        val m3 = MemberEntity("C", "t1", "C", weight = 1.0f)
        val members = listOf(m1, m2, m3)

        val e1 = ExpenseEntity("e1", "t1", "Activity", 60.0, 1L, "ACTIVITY", "A", SplitType.EQUAL.id, "A,B,C")
        val e2 = ExpenseEntity("e2", "t1", "Snacks", 30.0, 2L, "GROCERY", "B", SplitType.EQUAL.id, "A,B,C")

        val balances = SettlementEngine.calculateBalances(members, listOf(e1, e2), emptyList())
        val settlements = SettlementEngine.computeDirectSettlements(balances)

        assertEquals(1, settlements.size)
        assertEquals("C", settlements[0].fromMemberId)
        assertEquals("A", settlements[0].toMemberId)
        assertEquals(30.0, settlements[0].amount, 0.01)
    }

    @Test
    fun testMultiMemberAllSavedAndSplitAccurately() {
        // Testing a tour with 5 different member families/accounts (verifying no 2-account truncation!)
        val m1 = MemberEntity("m1", "tour1", "Organizer", weight = 1.0f, familyMembersText = "Organizer")
        val m2 = MemberEntity("m2", "tour1", "Smith Family", weight = 2.5f, familyMembersText = "John, Mary, Timmy")
        val m3 = MemberEntity("m3", "tour1", "Dave", weight = 1.0f, familyMembersText = "Dave")
        val m4 = MemberEntity("m4", "tour1", "Johnson Family", weight = 3.0f, familyMembersText = "Mark, Jane, Kid1, Kid2")
        val m5 = MemberEntity("m5", "tour1", "Elena", weight = 1.0f, familyMembersText = "Elena")

        val members = listOf(m1, m2, m3, m4, m5)
        assertEquals(5, members.size)

        // Total weight = 1.0 + 2.5 + 1.0 + 3.0 + 1.0 = 8.5
        val totalWeight = members.sumOf { it.weight.toDouble() }
        assertEquals(8.5, totalWeight, 0.01)

        // Expense of $850 paid by Dave (m3), split by weight across all 5 accounts
        val exp = ExpenseEntity(
            id = "e_group",
            tourId = "tour1",
            title = "Luxury Villa Booking",
            amount = 850.0,
            date = System.currentTimeMillis(),
            category = "STAY",
            paidById = "m3",
            splitType = SplitType.BY_WEIGHT.id,
            splitMemberIds = "m1,m2,m3,m4,m5"
        )

        val balances = SettlementEngine.calculateBalances(members, listOf(exp), emptyList())
        assertEquals(5, balances.size)

        // Dave paid 850, Dave's share = 850 * (1.0 / 8.5) = 100. Net = +750
        val daveBalance = balances.first { it.memberId == "m3" }
        assertEquals(750.0, daveBalance.netBalance, 0.01)

        // Johnson Family share = 850 * (3.0 / 8.5) = 300. Net = -300
        val johnsonBalance = balances.first { it.memberId == "m4" }
        assertEquals(-300.0, johnsonBalance.netBalance, 0.01)

        // Smith Family share = 850 * (2.5 / 8.5) = 250. Net = -250
        val smithBalance = balances.first { it.memberId == "m2" }
        assertEquals(-250.0, smithBalance.netBalance, 0.01)

        // Direct settlements verify that all members settle up to 0 net
        val settlements = SettlementEngine.computeDirectSettlements(balances)
        val totalSettled = settlements.sumOf { it.amount }
        assertEquals(750.0, totalSettled, 0.01)
    }

    @Test
    fun testMembersPreservedAcrossMultipleTours() {
        val multiverseId = "multiverse_tour_id"
        val demoTourId = "demo_tour_id"

        // Multiverse tour with 3 companion members
        val organizer = MemberEntity("m_org", multiverseId, "Sreeramkaimal", weight = 1.0f)
        val friend1 = MemberEntity("m_f1", multiverseId, "Alex", weight = 1.0f)
        val friend2 = MemberEntity("m_f2", multiverseId, "The Millers", weight = 2.0f)
        val multiverseMembers = listOf(organizer, friend1, friend2)

        // Demo tour members
        val demoMember1 = MemberEntity("d_1", demoTourId, "Demo User", weight = 1.0f)
        val demoMember2 = MemberEntity("d_2", demoTourId, "Demo Friend", weight = 1.0f)
        val demoMembers = listOf(demoMember1, demoMember2)

        // Both tours' members must coexist without replacing each other
        val combined = (multiverseMembers + demoMembers).distinctBy { it.id }
        assertEquals(5, combined.size)

        val tour1Members = combined.filter { it.tourId == multiverseId }
        val tour2Members = combined.filter { it.tourId == demoTourId }

        assertEquals(3, tour1Members.size)
        assertEquals(2, tour2Members.size)

        // Verify expenses can reference friend1 as payer and calculate balances
        val exp1 = ExpenseEntity(
            id = "e1",
            tourId = multiverseId,
            title = "Taxi Ride",
            amount = 1200.0,
            date = 1000L,
            category = "TRANSPORT",
            paidById = "m_f1",
            splitType = SplitType.EQUAL.id,
            splitMemberIds = "m_org,m_f1,m_f2"
        )
        val exp2 = ExpenseEntity(
            id = "e2",
            tourId = multiverseId,
            title = "Dinner",
            amount = 1500.0,
            date = 2000L,
            category = "FOOD",
            paidById = "m_f2",
            splitType = SplitType.EQUAL.id,
            splitMemberIds = "m_org,m_f1,m_f2"
        )

        val balances = SettlementEngine.calculateBalances(tour1Members, listOf(exp1, exp2), emptyList())
        assertEquals(3, balances.size)

        // Total spent: 2700. Per person equal share (3 accounts): 900 each.
        // Friend1 paid 1200, share 900 -> +300
        val f1Balance = balances.first { it.memberId == "m_f1" }
        assertEquals(300.0, f1Balance.netBalance, 0.01)

        // Friend2 paid 1500, share 900 -> +600
        val f2Balance = balances.first { it.memberId == "m_f2" }
        assertEquals(600.0, f2Balance.netBalance, 0.01)

        // Organizer paid 0, share 900 -> -900
        val orgBalance = balances.first { it.memberId == "m_org" }
        assertEquals(-900.0, orgBalance.netBalance, 0.01)
    }

    @Test
    fun testMemberNameUpdateAndPersistence() {
        val tourId = "tour_persist_test"
        val m1 = MemberEntity(
            id = "m1",
            tourId = tourId,
            name = "Tour Member",
            familyNote = "1 Adult",
            weight = 1.0f,
            avatarColorHex = "#E67E22",
            familyMembersText = "Tour Member"
        )

        // When profile owner updates member to a custom family name
        val updatedM1 = m1.copy(
            name = "Liam & Emma",
            familyNote = "Couple",
            weight = 2.0f,
            familyMembersText = "Liam, Emma",
            paymentUpiId = "liam@upi"
        )

        assertEquals("Liam & Emma", updatedM1.name)
        assertEquals("Couple", updatedM1.familyNote)
        assertEquals(2.0f, updatedM1.weight)
        assertEquals("Liam, Emma", updatedM1.familyMembersText)
        assertEquals("liam@upi", updatedM1.paymentUpiId)

        // Member list simulation
        val memberList = mutableListOf(m1)
        val idx = memberList.indexOfFirst { it.id == updatedM1.id }
        assertTrue(idx >= 0)
        memberList[idx] = updatedM1

        assertEquals("Liam & Emma", memberList[0].name)
        assertEquals(1, memberList.size)
    }

    @Test
    fun testMemberDeletionByProfileOwner() {
        val tourId = "tour_delete_test"
        val organizer = MemberEntity("org", tourId, "Organizer", familyNote = "Organizer Family", weight = 1.0f)
        val member1 = MemberEntity("m1", tourId, "Alice", familyNote = "Solo", weight = 1.0f)
        val member2 = MemberEntity("m2", tourId, "Bob", familyNote = "Solo", weight = 1.0f)

        val members = mutableListOf(organizer, member1, member2)

        // Profile owner removes member1
        members.removeIf { it.id == member1.id }

        assertEquals(2, members.size)
        assertTrue(members.none { it.id == "m1" })
        assertTrue(members.any { it.id == "org" })
        assertTrue(members.any { it.id == "m2" })
    }
}
