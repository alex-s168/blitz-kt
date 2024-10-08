package blitz.parse

import blitz.collections.RefVec
import blitz.parse.comb2.*
import org.json.JSONObject
import kotlin.math.min
import kotlin.system.measureNanoTime

object JSON {

    val jsonBool: Parser<Char, Element> = choose {
        it(mapValue(seq("true".toList())) { Element.newBool(true) })
        it(mapValue(seq("false".toList())) { Element.newBool(false) })
    }

    val jsonNull: Parser<Char, Element> =
        mapValue(seq("null".toList())) { Element.newNull() }

    val jsonNum: Parser<Char, Element> =
        mapValue(floatLit, Element::newNum)

    val jsonString: Parser<Char, Element> =
        mapValue(stringLit, Element::newStr)

    val jsonElement = futureRec { jsonElement: Parser<Char, Element> ->

        val jsonArray: Parser<Char, Element> =
            thenIgnore(
                thenIgnore(
                    thenOverwrite(just('['),
                        mapValue(delimitedBy(jsonElement, just(',')), Element::newArr)),
                whitespaces),
            just(']')
            )

        val jsonObj: Parser<Char, Element> =
            mapValue(thenIgnore(thenIgnore(thenOverwrite(
                just('{'),
                delimitedBy(
                    then(
                        thenIgnore(
                            thenIgnore(
                                thenOverwrite(
                                    whitespaces,
                                    stringLit),
                                whitespaces),
                            just(':')),
                        jsonElement),
                    just(','))),
                whitespaces),
                just('}'))) { Element.newObj(it.toMap()) }

        thenIgnore(thenOverwrite(
            whitespaces,
            choose {
                it(jsonArray)
                it(jsonNum)
                it(jsonString)
                it(jsonObj)
                it(jsonBool)
                it(jsonNull)
            }),
            whitespaces)

    }

    class Element(
        @JvmField val kind: Int,
        @JvmField val _boxed: Any? = null,
        @JvmField val _num: Double = 0.0,
        @JvmField val _bool: Boolean = false,
    ) {
        companion object {
            const val NUM = 0
            const val BOOL = 1
            const val NULL = 2
            const val ARR = 3
            const val STR = 4
            const val OBJ = 5

            inline fun newNum(v: Double): Element =
                Element(NUM, _num = v)
            inline fun newBool(v: Boolean): Element =
                Element(BOOL, _bool = v)
            inline fun newNull(): Element =
                Element(NULL)
            inline fun newArr(v: RefVec<Element>): Element =
                Element(ARR, _boxed = v)
            inline fun newStr(v: String): Element =
                Element(STR, _boxed = v)
            inline fun newObj(v: Map<String, Element>): Element =
                Element(OBJ, _boxed = v)
        }
    }
    
    inline fun Element.uncheckedAsNum(): Double =
        _num
    inline fun Element.uncheckedAsBool(): Boolean =
        _bool
    inline fun Element.uncheckedAsArr(): RefVec<Element> =
        _boxed as RefVec<Element>
    inline fun Element.uncheckedAsStr(): String =
        _boxed as String
    inline fun Element.uncheckedAsObj(): Map<String, Element> =
        _boxed as Map<String, Element>

    inline fun Element.asNum(): Double {
        require(kind == Element.NUM) { "Element is not a Number" }
        return _num
    }

    inline fun Element.asBool(): Boolean {
        require(kind == Element.BOOL) { "Element is not a Boolean" }
        return _bool
    }

    inline fun Element.asArr(): RefVec<Element> {
        require(kind == Element.ARR) { "Element is not an Array" }
        return _boxed as RefVec<Element>
    }

    inline fun Element.asStr(): String {
        require(kind == Element.STR) { "Element is not a String" }
        return _boxed as String
    }

    inline fun Element.asObj(): Map<String, Element> {
        require(kind == Element.OBJ) { "Element is not an Object" }
        return _boxed as Map<String, Element>
    }

    fun parse(string: String): ParseResult<Element> {
        val ctx = ParseCtx(string.toList(), 0)
        val v = jsonElement(ctx)
        return v
    }
}

fun main() {
    val json = """
{
 "clinical_study": {
  "brief_summary": {
   "textblock": "CLEAR SYNERGY is an international multi center 2x2 randomized placebo controlled trial of"
  },
  "brief_title": "CLEAR SYNERGY Neutrophil Substudy",
  "overall_status": "Recruiting",
  "eligibility": {
   "study_pop": {
    "textblock": "Patients who are randomized to the drug RCT portion of the CLEAR SYNERGY (OASIS 9) trial"
   },
   "minimum_age": "19 Years",
   "sampling_method": "Non-Probability Sample",
   "gender": "All",
   "criteria": {
    "textblock": "Inclusion Criteria:"
   },
   "healthy_volunteers": "No",
   "maximum_age": "110 Years"
  },
  "number_of_groups": "2",
  "source": "NYU Langone Health",
  "location_countries": {
   "country": "United States"
  },
  "study_design_info": {
   "time_perspective": "Prospective",
   "observational_model": "Other"
  },
  "last_update_submitted_qc": "September 10, 2019",
  "intervention_browse": {
   "mesh_term": "Colchicine"
  },
  "official_title": "Studies on the Effects of Colchicine on Neutrophil Biology in Acute Myocardial Infarction: A Substudy of the CLEAR SYNERGY (OASIS 9) Trial",
  "primary_completion_date": {
   "type": "Anticipated",
   "content": "February 1, 2021"
  },
  "sponsors": {
   "lead_sponsor": {
    "agency_class": "Other",
    "agency": "NYU Langone Health"
   },
   "collaborator": [
    {
     "agency_class": "Other",
     "agency": "Population Health Research Institute"
    },
    {
     "agency_class": "NIH",
     "agency": "National Heart, Lung, and Blood Institute (NHLBI)"
    }
   ]
  },
  "overall_official": {
   "role": "Principal Investigator",
   "affiliation": "NYU School of Medicine",
   "last_name": "Binita Shah, MD"
  },
  "overall_contact_backup": {
   "last_name": "Binita Shah, MD"
  },
  "condition_browse": {
   "mesh_term": [
    "Myocardial Infarction",
    "ST Elevation Myocardial Infarction",
    "Infarction"
   ]
  },
  "overall_contact": {
   "phone": "646-501-9648",
   "last_name": "Fatmira Curovic",
   "email": "fatmira.curovic@nyumc.org"
  },
  "responsible_party": {
   "responsible_party_type": "Principal Investigator",
   "investigator_title": "Assistant Professor of Medicine",
   "investigator_full_name": "Binita Shah",
   "investigator_affiliation": "NYU Langone Health"
  },
  "study_first_submitted_qc": "March 12, 2019",
  "start_date": {
   "type": "Actual",
   "content": "March 4, 2019"
  },
  "has_expanded_access": "No",
  "study_first_posted": {
   "type": "Actual",
   "content": "March 14, 2019"
  },
  "arm_group": [
   {
    "arm_group_label": "Colchicine"
   },
   {
    "arm_group_label": "Placebo"
   }
  ],
  "primary_outcome": {
   "measure": "soluble L-selectin",
   "time_frame": "between baseline and 3 months",
   "description": "Change in soluble L-selectin between baseline and 3 mo after STEMI in the placebo vs. colchicine groups."
  },
  "secondary_outcome": [
   {
    "measure": "Other soluble markers of neutrophil activity",
    "time_frame": "between baseline and 3 months",
    "description": "Other markers of neutrophil activity will be evaluated at baseline and 3 months after STEMI (myeloperoxidase, matrix metalloproteinase-9, neutrophil gelatinase-associated lipocalin, neutrophil elastase, intercellular/vascular cellular adhesion molecules)"
   },
   {
    "measure": "Markers of systemic inflammation",
    "time_frame": "between baseline and 3 months",
    "description": "Markers of systemic inflammation will be evaluated at baseline and 3 months after STEMI (high sensitive CRP, IL-1β)"
   },
   {
    "measure": "Neutrophil-driven responses that may further propagate injury",
    "time_frame": "between baseline and 3 months",
    "description": "Neutrophil-driven responses that may further propagate injury will be evaluated at baseline and 3 months after STEMI (neutrophil extracellular traps, neutrophil-derived microparticles)"
   }
  ],
  "oversight_info": {
   "is_fda_regulated_drug": "No",
   "is_fda_regulated_device": "No",
   "has_dmc": "No"
  },
  "last_update_posted": {
   "type": "Actual",
   "content": "September 12, 2019"
  },
  "id_info": {
   "nct_id": "NCT03874338",
   "org_study_id": "18-01323",
   "secondary_id": "1R01HL146206"
  },
  "enrollment": {
   "type": "Anticipated",
   "content": "670"
  },
  "study_first_submitted": "March 12, 2019",
  "condition": [
   "Neutrophils.Hypersegmented | Bld-Ser-Plas",
   "STEMI - ST Elevation Myocardial Infarction"
  ],
  "study_type": "Observational",
  "required_header": {
   "download_date": "ClinicalTrials.gov processed this data on July 19, 2020",
   "link_text": "Link to the current ClinicalTrials.gov record.",
   "url": "https://clinicaltrials.gov/show/NCT03874338"
  },
  "last_update_submitted": "September 10, 2019",
  "completion_date": {
   "type": "Anticipated",
   "content": "February 1, 2022"
  },
  "location": {
   "contact": {
    "phone": "646-501-9648",
    "last_name": "Fatmira Curovic",
    "email": "fatmira.curovic@nyumc.org"
   },
   "facility": {
    "address": {
     "zip": "10016",
     "country": "United States",
     "city": "New York",
     "state": "New York"
    },
    "name": "NYU School of Medicine"
   },
   "status": "Recruiting",
   "contact_backup": {
    "last_name": "Binita Shah, MD"
   }
  },
  "intervention": {
   "intervention_type": "Drug",
   "arm_group_label": [
    "Colchicine",
    "Placebo"
   ],
   "description": "Participants in the main CLEAR SYNERGY trial are randomized to colchicine/spironolactone versus placebo in a 2x2 factorial design. The substudy is interested in the evaluation of biospecimens obtained from patients in the colchicine vs placebo group.",
   "intervention_name": "Colchicine Pill"
  },
  "patient_data": {
   "sharing_ipd": "No"
  },
  "verification_date": "September 2019"
 }
}
    """.trimIndent()

    var minAlex = Long.MAX_VALUE
    var minJson = Long.MAX_VALUE
    while (true) {
        minAlex = min(measureNanoTime { JSON.parse(json).a!! }, minAlex)
        minJson = min(measureNanoTime { JSONObject(json) }, minJson)
        println("alex: $minAlex ns, json-java: $minJson ns ; alex is ${ minJson.toFloat() / minAlex.toFloat() } times as fast as json-java")
    }
}