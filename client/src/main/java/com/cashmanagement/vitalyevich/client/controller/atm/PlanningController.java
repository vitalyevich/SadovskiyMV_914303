package com.cashmanagement.vitalyevich.client.controller.atm;

import com.cashmanagement.vitalyevich.client.config.Seance;
import com.cashmanagement.vitalyevich.client.model.*;
import com.cashmanagement.vitalyevich.client.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/planning")
@Controller
public class PlanningController {


    private Seance seance = Seance.getInstance();

    @Autowired
    private PlanningServiceImpl planningService;

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private AtmServiceImpl atmService;

    @Autowired
    private WithdrawalCashServiceImpl withdrawalCashService;

    @Autowired
    private FilterService filterService;

    @GetMapping("")
    public String planning(Model model) {

        List<PlanAtm> planAtms = (List<PlanAtm>) planningService.getPlans();

        if (planAtms == null) {
            return "/error/500";
        }

        List<Atm> atms = planAtms.stream()
                .map(PlanAtm::getAtm)
                .collect(Collectors.toList());


        for (PlanAtm planAtm : planAtms) {

            //
            Order order = orderService.getOrder(planAtm.getId());
            planAtm.setOrder(order);
            //


            planAtm.setListCassettes("");
            planAtm.setAmount(0);

            if (!planAtm.getCassettes().iterator().hasNext()) {
                planAtm.setListCassettes(" ");
            }
            for (Cassette cassette: planAtm.getCassettes())
            {
                planAtm.setListCassettes(planAtm.getListCassettes() +
                        cassette.getAmount() + " ("+cassette.getBanknote()+")" + ", ");
                planAtm.setAmount(planAtm.getAmount() + cassette.getAmount());
            }
        }

        model.addAttribute("plans", planAtms);

        if (planId != null) {

            getHistory();

            planAtms.get(planId-1).setMarked("marked");
            model.addAttribute("id", planId-1);
            PlanAtm planAtm = planningService.getPlan(planId);
            Order order = orderService.getOrder(planAtm.getId());
            planAtm.setOrder(order);
            model.addAttribute("text", planAtm.getAtm().getAtmUid()+", План. инкассация " + planAtm.getDate());
            model.addAttribute("disabled", false);
            model.addAttribute("marked", "marked");
            model.addAttribute("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

            if (forecast == false) {
                if (atmId != null) {
                    int sum = 0;
                    for (Cassette cassette : planAtm.getAtm().getCassettes()) {
                        cassette.setSumAmount(cassette.getAmount() * Integer.parseInt(cassette.getBanknote()));
                        sum += cassette.getSumAmount();
                    }

                    Set<Cassette> cassetteSet = planAtm.getAtm().getCassettes();
                    cassetteList = new ArrayList<>(cassetteSet);
                    Collections.sort(cassetteList, Comparator.comparing(Cassette::getId));

                    model.addAttribute("cassettes", cassetteList);
                    model.addAttribute("sum", sum);
                }
            } else {
                int sum = 0;
                for (Cassette cassette : cassetteList1) {
                    cassette.setSumAmount(cassette.getAmount() * Integer.parseInt(cassette.getBanknote()));
                    sum += cassette.getSumAmount();
                }

                Collections.sort(cassetteList1, Comparator.comparing(Cassette::getId));

                model.addAttribute("cassettes", cassetteList1);
                model.addAttribute("sum", sum);
                forecast = false;
            }



        } else {
            model.addAttribute("disabled", true);
        }

        model.addAttribute("headerText", "Планирование");
        model.addAttribute("headerPost", "Старший кассир " + seance.getUser().getFirstName());
        model.addAttribute("plan", planAtmArrayList);

        Sidebar sidebar = new Sidebar();
        sidebar.getDropDown("/planning", companyService, model);

        model.addAttribute("url", "/planning/create-order/confirm");

        filterService.getValues(model, "/planning", atmId, atms);

        return "planning";
    }

    private List<Atm> atms = new LinkedList<>();

    @GetMapping("/plan-cash")
    public String planCash(@RequestParam Integer rowId, Model model, RedirectAttributes rm) {

        atmId = rowId + 1;
        return "redirect:/planning#blackout-plan";
    }

    private List<Cassette> cassetteList = new LinkedList<>();
    private List<Cassette> cassetteList1 = new LinkedList<>();
    private boolean forecast = false;


    @GetMapping("/plan-cash/accept")
    public String accept() {

        PlanAtm planAtm = planningService.getPlan(atmId);
        Set<Cassette> cassettes = new LinkedHashSet<>();
        for (Cassette cassette : cassetteList1) {
            cassettes.add(cassette);
        }

        planAtm.setCassettes(cassettes);

        planAtm.setStatus("Рассчитан");
        planningService.updatePlanAtm(planAtm);

        return "redirect:/planning";
    }


    @GetMapping("/edit-plan-cash/accept")
    public String edit(Model model, RedirectAttributes rm) {

        PlanAtm planAtm = planningService.getPlan(atmId);
        Set<Cassette> cassettes = new LinkedHashSet<>();
        for (Cassette cassette: cassetteList) {
            cassettes.add(cassette);
        }
        planAtm.setCassettes(cassettes);

        planAtm.setStatus("Изменен");
        planningService.updatePlanAtm(planAtm);

        return "redirect:/planning";
    }

    @PostMapping("/plan-cash-function")
    public String function(@RequestParam String date, Model model, RedirectAttributes rm) {
        cassetteList1 = new LinkedList<>();
        forecast = true;

        PlanAtm planAtm = planningService.getPlan(atmId);
        List<WithdrawalCash> withdrawalCashes = (List<WithdrawalCash>) withdrawalCashService.getCash(atmId);

        if (planAtm == null) {
            return "/error/500";
        }

            Set<Cassette> cassettes = planAtm.getAtm().getCassettes();

            int total5Amount = withdrawalCashes.stream()
                    .filter(cash -> Integer.parseInt(cash.getCassette().getBanknote()) == 5)
                    .mapToInt(cash -> cash.getAmount())
                    .sum();


            int count5 = (int) withdrawalCashes.stream()
                    .filter(cash -> Integer.parseInt(cash.getCassette().getBanknote()) == 5)
                    .count();


        int average5Amount = 0;
        int average10Amount = 0;
        int average20Amount = 0;
        int average50Amount = 0;
        int average100Amount = 0;
        int average200Amount = 0;

            if (count5 != 0) {
                average5Amount = total5Amount / count5;
            }


            int total10Amount = (int) withdrawalCashes.stream()
                    .filter(cash -> Integer.parseInt(cash.getCassette().getBanknote()) == 10)
                    .mapToInt(cash -> cash.getAmount())
                    .sum();

            int count10 = (int) withdrawalCashes.stream()
                    .filter(cash -> Integer.parseInt(cash.getCassette().getBanknote()) == 10)
                    .count();

        if (count10 != 0) {
            average10Amount = total10Amount / count10;
        }

            int total20Amount = (int) withdrawalCashes.stream()
                    .filter(cash -> Integer.parseInt(cash.getCassette().getBanknote()) == 20)
                    .mapToInt(cash -> cash.getAmount())
                    .sum();

            int count20 = (int) withdrawalCashes.stream()
                    .filter(cash -> Integer.parseInt(cash.getCassette().getBanknote()) == 20)
                    .count();

        if (count20 != 0) {
            average20Amount = total20Amount / count20;
        }

            int total50Amount = (int) withdrawalCashes.stream()
                    .filter(cash -> Integer.parseInt(cash.getCassette().getBanknote()) == 50)
                    .mapToInt(cash -> cash.getAmount())
                    .sum();

            int count50 = (int) withdrawalCashes.stream()
                    .filter(cash -> Integer.parseInt(cash.getCassette().getBanknote()) == 50)
                    .count();

        if (count50 != 0) {
            average50Amount = total50Amount / count50;
        }

            int total100Amount = (int) withdrawalCashes.stream()
                    .filter(cash -> Integer.parseInt(cash.getCassette().getBanknote()) == 100)
                    .mapToInt(cash -> cash.getAmount())
                    .sum();

            int count100 = (int) withdrawalCashes.stream()
                    .filter(cash -> Integer.parseInt(cash.getCassette().getBanknote()) == 100)
                    .count();

        if (count100 != 0) {
            average100Amount = total100Amount / count100;
        }

            int total200Amount = (int) withdrawalCashes.stream()
                    .filter(cash -> Integer.parseInt(cash.getCassette().getBanknote()) == 200)
                    .mapToInt(cash -> cash.getAmount())
                    .sum();

            int count200 = (int) withdrawalCashes.stream()
                    .filter(cash -> Integer.parseInt(cash.getCassette().getBanknote()) == 200)
                    .count();

        if (count200 != 0) {
            average200Amount = total200Amount / count200;
        }

        try {

            Cassette cassette5 = cassettes.stream()
                    .filter(cassette -> Integer.parseInt(cassette.getBanknote()) == 5)
                    .findFirst()
                    .orElse(null);

            Cassette cassette10 = cassettes.stream()
                    .filter(cassette -> Integer.parseInt(cassette.getBanknote()) == 10)
                    .findFirst()
                    .orElse(null);

            Cassette cassette20 = cassettes.stream()
                    .filter(cassette -> Integer.parseInt(cassette.getBanknote()) == 20)
                    .findFirst()
                    .orElse(null);

            Cassette cassette50 = cassettes.stream()
                    .filter(cassette -> Integer.parseInt(cassette.getBanknote()) == 50)
                    .findFirst()
                    .orElse(null);

            Cassette cassette100 = cassettes.stream()
                    .filter(cassette -> Integer.parseInt(cassette.getBanknote()) == 100)
                    .findFirst()
                    .orElse(null);

            Cassette cassette200 = cassettes.stream()
                    .filter(cassette -> Integer.parseInt(cassette.getBanknote()) == 200)
                    .findFirst()
                    .orElse(null);

            cassette5.setAmount(average5Amount);
            cassette10.setAmount(average10Amount);
            cassette20.setAmount(average20Amount);
            cassette50.setAmount(average50Amount);
            cassette100.setAmount(average100Amount);
            cassette200.setAmount(average200Amount);

            cassetteList1.add(cassette5);
            cassetteList1.add(cassette10);
            cassetteList1.add(cassette20);
            cassetteList1.add(cassette50);
            cassetteList1.add(cassette100);
            cassetteList1.add(cassette200);

        } catch (NullPointerException e) {

        }

        return "redirect:/planning#blackout-plan";
    }

    @GetMapping("/edit-plan-cash")
    public String editPlanCash(@RequestParam Integer rowId, Model model, RedirectAttributes rm) {

        atmId = rowId + 1;
        return "redirect:/planning#blackout-edit";
    }

    @PostMapping("/create-order")
    public String createOrder(@RequestParam Integer rowId, RedirectAttributes rm) {

        List<PlanAtm> planAtms = (List<PlanAtm>) planningService.getPlans();
        if (planAtms == null) {
            return "/error/500";
        }

        rm.addFlashAttribute("url", "/planning/create-order/confirm");
        rm.addFlashAttribute("id", rowId);
        return "redirect:/planning#blackout-confirm";
    }

    @PostMapping("/create-order/confirm")
    public String create(@RequestParam Integer rowId) {

        PlanAtm planAtm = planningService.getPlan(++rowId);
        planAtm.setStatus("Передан на исполнение");
        planningService.updatePlanAtm(planAtm);

        Order order = new Order();
        order.setStage("Генерация заказа наличных денег");
        order.setStatus("Новый");
        order.setOrderDate(LocalDate.now());

        orderService.saveOrder(order, planAtm.getId(), planAtm.getUser().getId());

        return "redirect:/planning";
    }

    @PostMapping("/accept")
    public String accept(Model model, @RequestParam Integer rowId) {

        PlanAtm planAtm = planningService.getPlan(++rowId);
        planAtm.setStatus("Принят");

        planningService.updatePlanAtm(planAtm);

        return "redirect:/planning";
    }

    private List<PlanAtm> planAtmArrayList = new LinkedList<>();

    private Integer planId = 1;
    private Integer atmId = null;


    private void getHistory() {

        planAtmArrayList.clear();

        PlanAtm planAtm = planningService.getPlan(planId);
        planAtm.setParameter("ID объекта");
        planAtm.setValue(planAtm.getAtm().getAtmUid());
        planAtmArrayList.add(planAtm);

        planAtm = planningService.getPlan(planId);
        planAtm.setParameter("Заказ");
        planAtmArrayList.add(planAtm);

        planAtm = planningService.getPlan(planId);
        Order order = orderService.getOrder(planAtm.getId());
        planAtm.setParameter("Дата инкассации");
        try {
            planAtm.setValue(order.getOrderDate().format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
        } catch (NullPointerException e) {
        }
        planAtmArrayList.add(planAtm);

        planAtm = planningService.getPlan(planId);
        planAtm.setParameter("Статус");
        planAtm.setValue(planAtm.getStatus());
        planAtmArrayList.add(planAtm);

        int sum = 0;
        for (Cassette cassette: planAtm.getCassettes())
        {
            planAtm = planningService.getPlan(planId);
            if (sum == 0) {
                planAtm.setParameter("Валюта");
            }
            planAtm.setValue(cassette.getCurrency());
            planAtm.setBanknote(cassette.getBanknote());
            planAtm.setAmountCassette(cassette.getAmount().toString());
            planAtm.setSum(cassette.getAmount() * Integer.parseInt(cassette.getBanknote()));
            sum += planAtm.getSum();
            planAtmArrayList.add(planAtm);
        }

        planAtm = planningService.getPlan(planId);
        planAtm.setParameter("Всего");
        planAtm.setAmountCassette(null);
        planAtm.setBanknote(null);
        planAtm.setValue(planAtm.getCurrency());
        planAtm.setSum(sum);
        planAtmArrayList.add(planAtm);
    }

    @GetMapping("/{id}")
    public String plan(Model model, @PathVariable Integer id) {

        planId = id;

        return "redirect:/planning";
    }

}
