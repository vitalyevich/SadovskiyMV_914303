package com.cashmanagement.vitalyevich.client.controller.collection;

import com.cashmanagement.vitalyevich.client.config.Seance;
import com.cashmanagement.vitalyevich.client.controller.atm.Sidebar;
import com.cashmanagement.vitalyevich.client.model.*;
import com.cashmanagement.vitalyevich.client.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.swing.text.DateFormatter;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequestMapping("/planning-collection")
@Controller
public class PlanningCollectionController {

    private Seance seance = Seance.getInstance();

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private FilterService filterService;

    @GetMapping("")
    public String planningCollection(Model model) {

        Iterable<Company> companies = companyService.getCompany();
        Iterable<User> users = userService.getUsers();

        if (companies == null) {
            return "/error/500";
        }

        model.addAttribute("userList", userList);
        model.addAttribute("companies", companies);
        model.addAttribute("users", users);

        Iterable<BrigadeOrder> brigadeOrders = orderService.getBrigadeOrders();
///
        /*for (BrigadeOrder brigadeOrder : brigadeOrders) {
            brigadeOrder.getOrder().getPlan().setListCassettes("");
            brigadeOrder.getOrder().getPlan().setAmount(0);
            if (!brigadeOrder.getOrder().getPlan().getCassettes().iterator().hasNext()) {
                brigadeOrder.getOrder().getPlan().setListCassettes(" ");
            }
            for (Cassette cassette: brigadeOrder.getOrder().getPlan().getCassettes())
            {
                brigadeOrder.getOrder().getPlan().setListCassettes(brigadeOrder.getOrder().getPlan().getListCassettes() +
                        cassette.getAmount() + " ("+cassette.getBanknote()+")" + ", ");
                brigadeOrder.getOrder().getPlan().setAmount(brigadeOrder.getOrder().getPlan().getAmount() + cassette.getAmount());
            }
        }*/

        brigadeOrders.forEach(brigadeOrder -> {
            brigadeOrder.getOrder().getPlan().setListCassettes(
                    brigadeOrder.getOrder().getPlan().getCassettes()
                            .stream()
                            .map(cassette -> cassette.getAmount() + " (" + cassette.getBanknote() + ")")
                            .collect(Collectors.joining(", "))
            );
            brigadeOrder.getOrder().getPlan().setAmount(
                    brigadeOrder.getOrder().getPlan().getCassettes()
                            .stream()
                            .mapToInt(Cassette::getAmount)
                            .sum()
            );
        });
///
        try {
            List<Brigade> brigades = (List<Brigade>) userService.getBrigades();

            String brigadeUsers = "";

            for (Brigade brigade : brigades) {
                brigadeUsers = "";
                for (User user : brigade.getUsers()) {
                    brigadeUsers += user.getLastName() + " " + user.getFirstName() + "\n";
                }
                brigade.setBrigadeUsers(brigadeUsers);
            }
            model.addAttribute("brigades", brigades);

        }
        catch (NullPointerException e) {

        }

        model.addAttribute("brigadeOrders", brigadeOrders);

        model.addAttribute("headerText", "Планирование");
        model.addAttribute("headerPost", "Старший инкассатор " + seance.getUser().getFirstName());

        Sidebar sidebar = new Sidebar();
        sidebar.getDropDown("/planning-collection", companyService, model);

        model.addAttribute("url", "/planning-collection/cancel-order/confirm");

        List<Atm> atms = ((List<BrigadeOrder>)brigadeOrders).stream()
                .flatMap(brigadeOrder -> Stream.of(brigadeOrder.getOrder().getPlan().getAtm()))
                .collect(Collectors.toList());

        filterService.getValues(model, "/planning-collection", 0, atms);

        return "planning-collection";
    }

    @GetMapping("/{id}")
    public String atmPlanningCollection(Model model, @PathVariable Integer id) {

        return "redirect:/planning-collection";
    }


    @PostMapping("/count")
    public String count(@RequestParam Integer rowId) {

        BrigadeOrder brigadeOrder = orderService.getBrigadeOrder(rowId);

        if (brigadeOrder == null) {
            return "/error/500";
        }

        brigadeOrder.getOrder().setStatus("Рассчитан");

        orderService.updateOrder(brigadeOrder.getOrder(), brigadeOrder.getOrder().getPlan().getId(), brigadeOrder.getUser().getId());

        return "redirect:/planning-collection";
    }

    @PostMapping("/cancel-order")
    public String cancelOrder(@RequestParam Integer rowId, RedirectAttributes rm) {
        rm.addFlashAttribute("url", "/planning-collection/cancel-order/confirm");
        rm.addFlashAttribute("id", rowId);

        return "redirect:/planning-collection#blackout-confirm";
    }

    @PostMapping("/cancel-order/confirm")
    public String cancelConfirm(@RequestParam Integer rowId) {

        BrigadeOrder brigadeOrder = orderService.getBrigadeOrder(rowId);
        orderService.deleteOrder(brigadeOrder.getOrder().getId());

        return "redirect:/planning-collection";
    }

    @PostMapping("/create-brigade")
    public String createBrigade(@RequestParam Integer rowId) {
        id = rowId;
        return "redirect:/planning-collection#blackout-brigade";
    }

    private List<User> userList = new ArrayList<>();
    private Set<User> userSet = new LinkedHashSet<>();
    private Integer id = 0;

    @PostMapping("/create-brigade/add")
    public String add(@RequestParam Integer rowId) {
        User user = userService.getUser(rowId);
        userList.add(user);
        return "redirect:/planning-collection#blackout-brigade";
    }

    @PostMapping("/create-brigade/del")
    public String delete(@RequestParam int rowId) {
        userList.remove(rowId);
        return "redirect:/planning-collection#blackout-brigade";
    }

    @PostMapping("/create-brigade/create")
    public String create(@RequestParam(name = "company") int companyId, @RequestParam(required=false) boolean active, @RequestParam(name="brigadeName") String brigadeName) {

        Set<User> users = new LinkedHashSet<>();

        if (userList.size() < 2) {
            return "redirect:/planning-collection";
        }
        users.addAll(userList);

        Iterable<Brigade> brigades = userService.getBrigades();
        if (brigades == null) {
            return "/error/500";
        }

        Brigade brigade = userService.saveBrigade(new Brigade(brigadeName, active, users), companyId);

        BrigadeOrder brigadeOrder = orderService.getBrigadeOrder(id);
        brigadeOrder.setBrigade(brigade);

        orderService.updateBrigadeOrder(brigadeOrder);

        brigadeOrder.getOrder().setStatus("Назначена бригада");
        orderService.updateOrder(brigadeOrder.getOrder(), brigadeOrder.getOrder().getPlan().getId(), brigadeOrder.getUser().getId());


        return "redirect:/planning-collection";
    }

    @PostMapping("/disband")
    public String disband(@RequestParam Integer rowId) {
        BrigadeOrder brigadeOrder = orderService.getBrigadeOrder(rowId);

        orderService.deleteBrigadeInOrder(brigadeOrder);

        brigadeOrder.getOrder().setStatus("Рассчитан");
        orderService.updateOrder(brigadeOrder.getOrder(), brigadeOrder.getOrder().getPlan().getId(), brigadeOrder.getUser().getId());

        if (brigadeOrder.getBrigade() != null) {
            userService.deleteBrigade(brigadeOrder.getBrigade().getId());
        }

        return "redirect:/planning-collection";
    }

    @PostMapping("/approve")
    public String approve(@RequestParam Integer rowId) {

        BrigadeOrder brigadeOrder = orderService.getBrigadeOrder(rowId);
        brigadeOrder.getOrder().setStatus("Передан на исполнение");

        orderService.updateOrder(brigadeOrder.getOrder(), brigadeOrder.getOrder().getPlan().getId(), brigadeOrder.getUser().getId());

        return "redirect:/planning-collection";
    }
}
