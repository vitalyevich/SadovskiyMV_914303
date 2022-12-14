package com.cashmanagement.vitalyevich.client.model;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "atms", indexes = {
        @Index(name = "atms_atm_uid_key", columnList = "atm_uid", unique = true)
})
public class Atm extends ColorTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "atm_id", nullable = false)
    private Integer id;

    @Column(name = "atm_uid", nullable = false, length = 8)
    private String atmUid;

    @Column(name = "cash_state", nullable = false)
    private String cashState;

    @Column(name = "atm_state", nullable = false)
    private String atmState;

    private String listCassettes;

    private String currency;

    private Integer amount;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "home_num", nullable = false)
    private Integer homeNum;

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        if (!cassettes.iterator().hasNext()) {
            return " ";
        }
        return cassettes.iterator().next().getCurrency();
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getListCassettes() {
        return listCassettes;
    }

    public void setListCassettes(String listCassettes) {
        this.listCassettes = listCassettes;
    }

    private String company;

    public String getCompany() {
        if (!companies.iterator().hasNext()) {
            return " ";
        }
        return companies.iterator().next().getCompanyName();
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getHomeNum() {
        return homeNum;
    }

    public void setHomeNum(Integer homeNum) {
        this.homeNum = homeNum;
    }

    @ManyToMany
    @JoinTable(name = "atm_cassettes",
            joinColumns = @JoinColumn(name = "atm_id"),
            inverseJoinColumns = @JoinColumn(name = "cassette_id"))
    private Set<Cassette> cassettes = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "company_atms",
            joinColumns = @JoinColumn(name = "atm_id"),
            inverseJoinColumns = @JoinColumn(name = "company_id"))
    private Set<Company> companies = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "city_atms",
            joinColumns = @JoinColumn(name = "atm_id"),
            inverseJoinColumns = @JoinColumn(name = "city_id"))
    private Set<City> cities = new LinkedHashSet<>();

    public Set<City> getCities() {
        return cities;
    }

    public void setCities(Set<City> cities) {
        this.cities = cities;
    }

    @Override
    public String getColorFirst() {
        if (cashState == null) {
            return " ";
        }
        return cashState.equals("Нормальное") ? "#57DB4E" : "#FF3F3F";
    }

    @Override
    public String getColorSecond() {

        if (atmState == null) {
            return " ";
        }

        if (atmState.equals("Нормальный")) {
            return "#57DB4E";
        } else if (atmState.equals("Неопределённый")) {
            return "#F1CB00";
        } else {
            return "#FF3F3F";
        }
    }

    public String getCompanies() {
        if (!companies.iterator().hasNext()) {
            return " ";
        }
        return companies.iterator().next().getCompanyName();
    }

    public void setCompanies(Set<Company> companies) {
        this.companies = companies;
    }

    public Set<Cassette> getCassettes() {
        return cassettes;
    }

    public void setCassettes(Set<Cassette> cassettes) {
        this.cassettes = cassettes;
    }

    public String getAtmState() {
        return atmState;
    }

    public void setAtmState(String atmState) {
        this.atmState = atmState;
    }

    public String getCashState() {
        return cashState;
    }

    public void setCashState(String cashState) {
        this.cashState = cashState;
    }

    public String getAtmUid() {
        return atmUid;
    }

    public void setAtmUid(String atmUid) {
        this.atmUid = atmUid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}