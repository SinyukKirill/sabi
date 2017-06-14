/*
 * Copyright (c) 2017 by Stefan Schubert
 */

package de.bluewhale.sabi.persistence.model;

import javax.persistence.*;

/**
 *
 * User: Stefan
 * Date: 12.03.15
 */
@Table(name = "fish_catalogue", schema = "sabi")
@Entity
public class FishCatalogueEntity extends TracableEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @javax.persistence.Column(name = "id", nullable = false, insertable = true, updatable = true, length = 20, precision = 0)
    @Basic
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private String scientificName;

    @javax.persistence.Column(name = "scientific_name", nullable = true, insertable = true, updatable = true, length = 60, precision = 0)
    @Basic
    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    private String description;

    @javax.persistence.Column(name = "description", nullable = true, insertable = true, updatable = true, length = 400, precision = 0)
    @Basic
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String meerwasserwikiUrl;

    @javax.persistence.Column(name = "meerwasserwiki_url", nullable = true, insertable = true, updatable = true, length = 120, precision = 0)
    @Basic
    public String getMeerwasserwikiUrl() {
        return meerwasserwikiUrl;
    }

    public void setMeerwasserwikiUrl(String meerwasserwikiUrl) {
        this.meerwasserwikiUrl = meerwasserwikiUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FishCatalogueEntity that = (FishCatalogueEntity) o;

        if (id != that.id) return false;
        if (createdOn != null ? !createdOn.equals(that.createdOn) : that.createdOn != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (lastmodOn != null ? !lastmodOn.equals(that.lastmodOn) : that.lastmodOn != null) return false;
        if (meerwasserwikiUrl != null ? !meerwasserwikiUrl.equals(that.meerwasserwikiUrl) : that.meerwasserwikiUrl != null)
            return false;
        if (scientificName != null ? !scientificName.equals(that.scientificName) : that.scientificName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (scientificName != null ? scientificName.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (meerwasserwikiUrl != null ? meerwasserwikiUrl.hashCode() : 0);
        result = 31 * result + (createdOn != null ? createdOn.hashCode() : 0);
        result = 31 * result + (lastmodOn != null ? lastmodOn.hashCode() : 0);
        return result;
    }
}
