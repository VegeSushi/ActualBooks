package online.sushiware.actualBooks;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import java.util.ArrayList;
import java.util.List;

public class BookFactory {
    public static ItemStack createBook(String title, String content) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle(title);
        meta.setDisplayName(title);
        meta.setAuthor("Wikipedia API");

        List<String> pages = new ArrayList<>();
        String[] words = content.split(" ");
        StringBuilder page = new StringBuilder();

        for (String word : words) {
            if (page.length() + word.length() + 1 > 250) {
                pages.add(page.toString());
                page = new StringBuilder();
                if (pages.size() >= 100) break;
            }
            page.append(word).append(" ");
        }
        pages.add(page.toString());
        meta.setPages(pages);
        book.setItemMeta(meta);
        return book;
    }
}