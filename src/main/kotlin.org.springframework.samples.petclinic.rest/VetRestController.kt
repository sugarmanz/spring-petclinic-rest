/*
 * Copyright 2016-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.rest

import javax.transaction.Transactional
import javax.validation.Valid

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.samples.petclinic.model.Vet
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.* // ktlint-disable no-wildcard-imports
import org.springframework.web.util.UriComponentsBuilder

/**
 * @author Vitaliy Fedoriv
 *
 * Updated by Katie Levy 4/24/19
 */

@RestController
@CrossOrigin(exposedHeaders = ["errors, content-type"])
@RequestMapping("api/vets")
open class VetRestController(@Autowired private val clinicService: ClinicService) {

    @GetMapping("/")
    fun getAllVets(): ResponseEntity<Collection<Vet>> {
        val vets = arrayListOf<Vet>(*this.clinicService.findAllVets().toTypedArray())
        return if (vets.isEmpty()) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(vets, HttpStatus.OK)
    }

    @GetMapping("/{vetId}")
    fun getVet(@PathVariable("vetId") vetId: Int): ResponseEntity<Vet> {
        val vet = this.clinicService.findVetById(vetId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(vet, HttpStatus.OK)
    }

    @PostMapping("/")
    fun addVet(@RequestBody @Valid vet: Vet, bindingResult: BindingResult, ucBuilder: UriComponentsBuilder): ResponseEntity<Vet> {
        val headers = HttpHeaders()
        if (bindingResult.hasErrors()) {
            val errors = BindingErrorsResponse()
            BindingErrorsResponse().addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity(headers, HttpStatus.BAD_REQUEST)
        }
        this.clinicService.saveVet(vet)
        headers.location = ucBuilder.path("/api/vets/{id}").buildAndExpand(vet.id).toUri()
        return ResponseEntity(vet, headers, HttpStatus.CREATED)
    }

    @RequestMapping(value = ["/{vetId}"], method = [RequestMethod.PUT], produces = ["application/json; charset=utf-8"])
    fun updateVet(@PathVariable("vetId") vetId: Int, @RequestBody @Valid vet: Vet, bindingResult: BindingResult): ResponseEntity<Vet> {
        val headers = HttpHeaders()
        if (bindingResult.hasErrors()) {
            val errors = BindingErrorsResponse()
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity(headers, HttpStatus.BAD_REQUEST)
        }
        val currentVet = this.clinicService.findVetById(vetId)
            .apply {
                firstName = vet.firstName
                lastName = vet.lastName
                clearSpecialties()
                vet.specialties.forEach { addSpecialty(it) }
            }
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        this.clinicService.saveVet(currentVet)
        return ResponseEntity(currentVet, HttpStatus.NO_CONTENT)
    }

    @DeleteMapping("/{vetId}")
    @Transactional
    open fun deleteVet(@PathVariable("vetId") vetId: Int): ResponseEntity<Void> {
        val vet = this.clinicService.findVetById(vetId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        this.clinicService.deleteVet(vet)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
