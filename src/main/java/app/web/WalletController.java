package app.web;

import app.transaction.model.Transaction;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import app.web.mapper.DtoMapper;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import static app.security.SessionInterceptor.USER_ID_FROM_SESSION;

@Controller
@RequestMapping("wallets")
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;

    @Autowired
    public WalletController(WalletService walletService, UserService userService) {

        this.walletService = walletService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getWallets(HttpSession session) {

        UUID userId = (UUID) session.getAttribute(USER_ID_FROM_SESSION);
        User user = userService.getById(userId);

        Map<UUID, List<Transaction>> walletTransactions = walletService.getLastFourTransactionsForWallets(user.getWallets());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("lastFourTransactions", walletTransactions);
        modelAndView.setViewName("wallets");

        return modelAndView;
    }

    @GetMapping("/{walletId}/top-up")
    public ModelAndView topUpWallet(@PathVariable UUID walletId, @RequestParam("amount") BigDecimal amount, HttpSession session) {

        UUID userId = (UUID) session.getAttribute(USER_ID_FROM_SESSION);
        User user = userService.getById(userId);

        Transaction transaction = walletService.topUp(user, walletId, amount);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("transactionResult", DtoMapper.toTransactionResult(transaction));
        modelAndView.setViewName("transaction-result");

        return modelAndView;
    }

    @GetMapping("/{walletId}/switch-status")
    public ModelAndView switchWalletStatus(@PathVariable UUID walletId, HttpSession session) {

        UUID userId = (UUID) session.getAttribute(USER_ID_FROM_SESSION);
        User user = userService.getById(userId);

        walletService.switchStatus(user, walletId);
        Map<UUID, List<Transaction>> walletTransactions = walletService.getLastFourTransactionsForWallets(user.getWallets());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("lastFourTransactions", walletTransactions);
        modelAndView.setViewName("wallets");

        return modelAndView;
    }

    @GetMapping("/new-wallet")
    public ModelAndView createNewWallet(HttpSession session) {

        UUID userId = (UUID) session.getAttribute(USER_ID_FROM_SESSION);
        User user = userService.getById(userId);

        walletService.createNewWallet(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("home");

        return modelAndView;
    }
}