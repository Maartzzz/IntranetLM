package com.lmoving.intranet.provider.dto;

import jakarta.validation.constraints.NotNull;

public record ProviderStatusRequest(@NotNull Boolean active) {}
