package com.example.prsupabase1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prsupabase1.R;
import com.example.prsupabase1.Tasks;
import java.util.ArrayList;
import java.util.List;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {

    private List<Tasks> tasks = new ArrayList<>();

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Tasks task = tasks.get(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription());
        holder.checkBoxCompleted.setChecked(task.isCompleted());

        // Форматируем дату (убираем лишнюю информацию)
        if (task.getCreatedAt() != null) {
            String date = task.getCreatedAt().substring(0, 10); // Берем только дату
            holder.tvCreatedAt.setText("Создано: " + date);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Tasks> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDescription;
        CheckBox checkBoxCompleted;
        TextView tvCreatedAt;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxCompleted);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }
    }
}