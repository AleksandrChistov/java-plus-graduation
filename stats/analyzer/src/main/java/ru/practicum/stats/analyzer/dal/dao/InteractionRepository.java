package ru.practicum.stats.analyzer.dal.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.stats.analyzer.dal.model.interaction.Interaction;
import ru.practicum.stats.analyzer.dal.model.interaction.InteractionId;

public interface InteractionRepository extends JpaRepository<Interaction, InteractionId> {
}
