package com.isaakhanimann.healthassistant.ui.addingestion.search

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.isaakhanimann.healthassistant.data.room.experiences.entities.SubstanceColor
import com.isaakhanimann.healthassistant.data.substances.AdministrationRoute

class AddIngestionSearchScreenProvider : PreviewParameterProvider<List<PreviousSubstance>> {
    override val values: Sequence<List<PreviousSubstance>> = sequenceOf(
        listOf(
            PreviousSubstance(
                color = SubstanceColor.PINK,
                substanceName = "MDMA",
                isCustom = false,
                routesWithDoses = listOf(
                    RouteWithDoses(
                        route = AdministrationRoute.ORAL,
                        doses = listOf(
                            PreviousDose(
                                dose = 50.0,
                                unit = "mg",
                                isEstimate = false
                            ),
                            PreviousDose(
                                dose = 100.0,
                                unit = "mg",
                                isEstimate = false
                            ),
                            PreviousDose(
                                dose = null,
                                unit = "mg",
                                isEstimate = false
                            )
                        )
                    )
                )
            ),
            PreviousSubstance(
                color = SubstanceColor.BLUE,
                substanceName = "Amphetamine",
                isCustom = false,
                routesWithDoses = listOf(
                    RouteWithDoses(
                        route = AdministrationRoute.INSUFFLATED,
                        doses = listOf(
                            PreviousDose(
                                dose = 10.0,
                                unit = "mg",
                                isEstimate = false
                            ),
                            PreviousDose(
                                dose = 20.0,
                                unit = "mg",
                                isEstimate = false
                            ),
                            PreviousDose(
                                dose = null,
                                unit = "mg",
                                isEstimate = false
                            )
                        )
                    ),
                    RouteWithDoses(
                        route = AdministrationRoute.ORAL,
                        doses = listOf(
                            PreviousDose(
                                dose = 30.0,
                                unit = "mg",
                                isEstimate = false
                            )
                        )
                    )
                )
            )
        )
    )
}