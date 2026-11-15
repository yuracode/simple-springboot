package com.example.demo.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Category;
import com.example.demo.model.CategoryTotal;
import com.example.demo.model.Pocket;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// お小遣い帳：記録の一覧・登録・編集・削除・月の集計（URL 設計 No.1〜7）
// リポジトリやサービスは作らず、JdbcTemplate をコントローラで直接使う
@Controller
@RequiredArgsConstructor
public class PocketController {

    private final JdbcTemplate jdbcTemplate;

    // SQL の結果 1 行を Pocket 1 つに詰める（rs.getXxx("カラム名") で 1 列ずつ取り出す）
    private final RowMapper<Pocket> pocketMapper = (rs, rowNum) -> {
        Pocket pocket = new Pocket();
        pocket.setId(rs.getLong("id"));
        pocket.setEntryDate(rs.getDate("entry_date").toLocalDate());   // java.sql.Date → LocalDate
        pocket.setType(rs.getString("type"));
        pocket.setCategoryId(rs.getInt("category_id"));
        pocket.setCategoryName(rs.getString("category_name"));
        pocket.setAmount(rs.getInt("amount"));
        pocket.setMemo(rs.getString("memo"));
        return pocket;
    };

    // SQL の結果 1 行を Category 1 つに詰める（セレクトボックス用。件数は使わない）
    private final RowMapper<Category> categoryMapper = (rs, rowNum) -> {
        Category category = new Category();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setSortOrder(rs.getInt("sort_order"));
        return category;
    };

    // SQL の結果 1 行を CategoryTotal 1 つに詰める（月の集計のカテゴリ別の支出）
    private final RowMapper<CategoryTotal> categoryTotalMapper = (rs, rowNum) -> {
        CategoryTotal total = new CategoryTotal();
        total.setCategory(rs.getString("category"));
        total.setCount(rs.getInt("count"));
        total.setTotal(rs.getInt("total"));
        return total;
    };

    // No.1 一覧・残高
    @GetMapping("/pocket")
    public String list(@RequestParam(required = false) String month, Model model) {
        YearMonth ym = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);

        List<Pocket> entries = jdbcTemplate.query("""
                SELECT e.id, e.entry_date, e.type, e.category_id, c.name AS category_name, e.amount, e.memo
                FROM pocket_entries e
                JOIN categories c ON c.id = e.category_id
                WHERE e.entry_date >= ? AND e.entry_date < ?
                ORDER BY e.entry_date, e.id
                """, pocketMapper,
                ym.atDay(1), ym.plusMonths(1).atDay(1));

        // 残高は全期間。記録が 0 件だと SUM が NULL になるので COALESCE で 0 にする
        Integer balance = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(CASE WHEN type = '収入' THEN amount ELSE -amount END), 0) FROM pocket_entries",
                Integer.class);

        model.addAttribute("entries", entries);
        model.addAttribute("balance", balance);
        model.addAttribute("month", ym);
        return "pocket/list";
    }

    // No.2 登録フォーム（初期値：日付は今日、種類は支出）
    @GetMapping("/pocket/new")
    public String newForm(Model model) {
        Pocket pocket = new Pocket();
        pocket.setEntryDate(LocalDate.now());
        pocket.setType("支出");
        model.addAttribute("pocket", pocket);
        model.addAttribute("categories", findCategories());   // セレクトボックスの選択肢
        return "pocket/form";
    }

    // No.3 登録
    @PostMapping("/pocket")
    public String create(@ModelAttribute Pocket pocket, RedirectAttributes redirectAttributes) {
        jdbcTemplate.update(
                "INSERT INTO pocket_entries (entry_date, type, category_id, amount, memo) VALUES (?, ?, ?, ?, ?)",
                pocket.getEntryDate(), pocket.getType(), pocket.getCategoryId(), pocket.getAmount(), pocket.getMemo());
        redirectAttributes.addFlashAttribute("message", categoryName(pocket.getCategoryId())
                + " " + pocket.getAmount() + "円を登録しました");
        return "redirect:/pocket";
    }

    // No.4 編集フォーム
    @GetMapping("/pocket/{id}/edit")
    public String editForm(@PathVariable int id, Model model, HttpServletResponse response) {
        List<Pocket> list = findPocket(id);
        if (list.isEmpty()) {
            return notFound(id, model, response);
        }
        model.addAttribute("pocket", list.get(0));   // 1 件だけを渡す
        model.addAttribute("categories", findCategories());
        return "pocket/form";
    }

    // No.5 更新
    @PostMapping("/pocket/{id}")
    public String update(@PathVariable int id, @ModelAttribute Pocket pocket, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        // どの記録を更新するかは URL の id で決める。更新された行数が 0 ならその ID は無い
        int count = jdbcTemplate.update(
                "UPDATE pocket_entries SET entry_date = ?, type = ?, category_id = ?, amount = ?, memo = ? WHERE id = ?",
                pocket.getEntryDate(), pocket.getType(), pocket.getCategoryId(), pocket.getAmount(), pocket.getMemo(), id);
        if (count == 0) {
            return notFound(id, model, response);
        }
        redirectAttributes.addFlashAttribute("message", categoryName(pocket.getCategoryId())
                + " " + pocket.getAmount() + "円に更新しました");
        return "redirect:/pocket";
    }

    // No.6 削除
    @PostMapping("/pocket/{id}/delete")
    public String delete(@PathVariable int id, Model model, RedirectAttributes redirectAttributes,
            HttpServletResponse response) {
        List<Pocket> list = findPocket(id);   // メッセージ用に、消す前に読んでおく
        if (list.isEmpty()) {
            return notFound(id, model, response);
        }
        Pocket pocket = list.get(0);
        jdbcTemplate.update("DELETE FROM pocket_entries WHERE id = ?", id);
        redirectAttributes.addFlashAttribute("message", pocket.getCategoryName()
                + " " + pocket.getAmount() + "円を削除しました");
        return "redirect:/pocket";
    }

    // No.7 月の集計
    @GetMapping("/pocket/summary")
    public String summary(@RequestParam(required = false) String month, Model model) {
        YearMonth ym = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.plusMonths(1).atDay(1);

        // 収入・支出の合計（その月に記録が無いと SUM が NULL になるので COALESCE で 0 にする）
        String sumSql = "SELECT COALESCE(SUM(amount), 0) FROM pocket_entries"
                + " WHERE type = ? AND entry_date >= ? AND entry_date < ?";
        Integer incomeTotal = jdbcTemplate.queryForObject(sumSql, Integer.class, "収入", from, to);
        Integer expenseTotal = jdbcTemplate.queryForObject(sumSql, Integer.class, "支出", from, to);

        // カテゴリ別の支出（1 行が CategoryTotal 1 つになる）
        List<CategoryTotal> categoryTotals = jdbcTemplate.query("""
                SELECT c.name AS category, COUNT(*) AS count, SUM(e.amount) AS total
                FROM pocket_entries e
                JOIN categories c ON c.id = e.category_id
                WHERE e.type = '支出' AND e.entry_date >= ? AND e.entry_date < ?
                GROUP BY c.id, c.name
                ORDER BY total DESC
                """, categoryTotalMapper, from, to);

        model.addAttribute("month", ym);
        model.addAttribute("incomeTotal", incomeTotal);
        model.addAttribute("expenseTotal", expenseTotal);
        model.addAttribute("difference", incomeTotal - expenseTotal);
        model.addAttribute("categoryTotals", categoryTotals);
        model.addAttribute("prevMonth", ym.minusMonths(1));
        model.addAttribute("nextMonth", ym.plusMonths(1));
        return "pocket/summary";
    }

    // ---- ここから下は、このクラスの中だけで使う小さな部品 ----

    // 1 件取得。結果は 0 件か 1 件のリスト（呼び出し側で isEmpty() と get(0) を使う）
    private List<Pocket> findPocket(int id) {
        return jdbcTemplate.query("""
                SELECT e.id, e.entry_date, e.type, e.category_id, c.name AS category_name, e.amount, e.memo
                FROM pocket_entries e
                JOIN categories c ON c.id = e.category_id
                WHERE e.id = ?
                """, pocketMapper, id);
    }

    // セレクトボックスの選択肢（表示順）
    private List<Category> findCategories() {
        return jdbcTemplate.query("SELECT * FROM categories ORDER BY sort_order, id",
                categoryMapper);
    }

    // メッセージ用のカテゴリ名
    private String categoryName(Integer categoryId) {
        return jdbcTemplate.queryForObject("SELECT name FROM categories WHERE id = ?", String.class, categoryId);
    }

    // 404 画面（リダイレクトせず、ステータス 404 でこの画面を返す）
    private String notFound(int id, Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("errorMessage", "記録が見つかりません（ID：" + id + "）");
        return "pocket/not-found";
    }
}
