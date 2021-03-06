/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sis.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "building")
@Getter
@Setter
public class Building extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Column(name = "code")
    private String code;

    @Column(name = "name_ar")
    private String nameAr;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "status")
    private int status;

    @JoinColumn(name = "college_id", referencedColumnName = "id")
    @ManyToOne
    private College collegeId;

    @OneToMany(mappedBy = "building")
    private Collection<Classroom> classroomCollection;

    @OneToMany(mappedBy = "building")
    private Collection<Timetable> timetables;
}
