package com.kkt.worthcalculation.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "Test")
class Test(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val t1: String,
    val t2: String,
    val d1: Timestamp? = null,
    val n1: BigDecimal? = null
)

@Repository
interface TestRepository : JpaRepository<Test, Long>