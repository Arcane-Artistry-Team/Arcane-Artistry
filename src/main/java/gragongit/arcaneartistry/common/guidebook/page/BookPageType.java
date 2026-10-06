package gragongit.arcaneartistry.common.guidebook.page;

import com.mojang.serialization.MapCodec;

public record BookPageType<T extends BookPage>(MapCodec<T> codec) {
}
