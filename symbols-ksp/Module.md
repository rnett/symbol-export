# Module symbols-ksp

Integration between the generated symbol types and KSP.

WARNING: Because there is no easy way to test KSP libraries, this integration is much more experimental than the others.

# Package dev.rnett.symbolexport.symbol.ksp

Methods to compare `KSName` and other KSP types to exported symbols.

NOTE: There is no `Symbol.asKsName()` like there is for the other integrations because `KSName` does not implement equals, so using it would be a trap.
