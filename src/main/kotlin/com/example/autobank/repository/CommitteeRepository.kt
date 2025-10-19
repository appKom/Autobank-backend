package com.example.autobank.repository;

import com.example.autobank.data.models.Committee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommitteeRepository : JpaRepository<Committee, String> {


}
