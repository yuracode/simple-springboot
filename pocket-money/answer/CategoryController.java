package com.example.demo.controller;

import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Category;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// お小遣い帳：カテゴリの一覧・登録・編集・削除（URL 設計 No.8〜13）
// リポジトリやサービスは作らず、JdbcTemplate をコントローラで直接使う
@Controller
@RequiredArgsConstructor
public class CategoryController {

    private final JdbcTemplate jdbcTemplate;

    // SQL の結果 1 行を Category 1 つに詰める（rs.getXxx("カラム名") で 1 列ずつ取り出す）
    private final RowMapper<Category> categoryMapper = (rs, rowNum) -> {
        Category category = new Category();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setSortOrder(rs.getInt("sort_order"));
        category.setEntryCount(rs.getInt("entry_count"));   // LEFT JOIN で数えた記録の件数
        return category;
    };

    // No.8 一覧（記録の件数つき。LEFT JOIN で 0 件のカテゴリも残す）
    @GetMapping("/categories")
    public String list(Model model) {
        List<Category> categories = jdbcTemplate.query("""
                SELECT c.id, c.name, c.sort_order, COUNT(e.id) AS entry_count
                FROM categories c
                LEFT JOIN pocket_entries e ON e.category_id = c.id
                GROUP BY c.id, c.name, c.sort_order
                ORDER BY c.sort_order, c.id
                """, categoryMapper);
        model.addAttribute("categories", categories);
        return "category/list";
    }

    // No.9 登録フォーム（表示順の初期値は「最大値 + 1」）
    @GetMapping("/categories/new")
    public String newForm(Model model) {
        Category category = new Category();
        category.setSortOrder(jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(sort_order), 0) + 1 FROM categories", Integer.class));
        model.addAttribute("category", category);
        return "category/form";
    }

    // No.10 登録
    @PostMapping("/categories")
    public String create(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        jdbcTemplate.update("INSERT INTO categories (name, sort_order) VALUES (?, ?)",
                category.getName(), category.getSortOrder());
        redirectAttributes.addFlashAttribute("message", category.getName() + "を登録しました");
        return "redirect:/categories";
    }

    // No.11 編集フォーム
    @GetMapping("/categories/{id}/edit")
    public String editForm(@PathVariable int id, Model model, HttpServletResponse response) {
        List<Category> list = findCategory(id);
        if (list.isEmpty()) {
            return notFound(id, model, response);
        }
        model.addAttribute("category", list.get(0));   // 1 件だけを渡す
        return "category/form";
    }

    // No.12 更新
    @PostMapping("/categories/{id}")
    public String update(@PathVariable int id, @ModelAttribute Category category, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        // どのカテゴリを更新するかは URL の id で決める。更新された行数が 0 ならその ID は無い
        int count = jdbcTemplate.update("UPDATE categories SET name = ?, sort_order = ? WHERE id = ?",
                category.getName(), category.getSortOrder(), id);
        if (count == 0) {
            return notFound(id, model, response);
        }
        redirectAttributes.addFlashAttribute("message", category.getName() + "に更新しました");
        return "redirect:/categories";
    }

    // No.13 削除（記録で使われていたら消さない）
    @PostMapping("/categories/{id}/delete")
    public String delete(@PathVariable int id, Model model, RedirectAttributes redirectAttributes,
            HttpServletResponse response) {
        List<Category> list = findCategory(id);
        if (list.isEmpty()) {
            return notFound(id, model, response);
        }
        Category category = list.get(0);

        // 外部キー制約があるので、使われているカテゴリを消そうとすると DB がエラーになる
        if (category.getEntryCount() > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", category.getName()
                    + "は " + category.getEntryCount() + " 件の記録で使われているため削除できません");
            return "redirect:/categories";
        }
        jdbcTemplate.update("DELETE FROM categories WHERE id = ?", id);
        redirectAttributes.addFlashAttribute("message", category.getName() + "を削除しました");
        return "redirect:/categories";
    }

    // ---- ここから下は、このクラスの中だけで使う小さな部品 ----

    // 1 件取得（記録の件数つき）。結果は 0 件か 1 件のリスト（呼び出し側で isEmpty() と get(0) を使う）
    private List<Category> findCategory(int id) {
        return jdbcTemplate.query("""
                SELECT c.id, c.name, c.sort_order, COUNT(e.id) AS entry_count
                FROM categories c
                LEFT JOIN pocket_entries e ON e.category_id = c.id
                WHERE c.id = ?
                GROUP BY c.id, c.name, c.sort_order
                """, categoryMapper, id);
    }

    // 404 画面（記録と同じテンプレートを使い、メッセージだけ変える）
    private String notFound(int id, Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("errorMessage", "カテゴリが見つかりません（ID：" + id + "）");
        return "pocket/not-found";
    }
}
