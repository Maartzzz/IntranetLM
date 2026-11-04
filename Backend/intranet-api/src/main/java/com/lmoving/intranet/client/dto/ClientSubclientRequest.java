package com.lmoving.intranet.client.dto;
import jakarta.validation.constraints.NotNull;
public record ClientSubclientRequest(@NotNull Long subclientId) {}
