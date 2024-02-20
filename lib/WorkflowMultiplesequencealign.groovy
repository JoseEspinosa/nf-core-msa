//
// This file holds several functions specific to the workflow/multiplesequencealign.nf in the nf-core/multiplesequencealign pipeline
//

import nextflow.Nextflow
import groovy.text.SimpleTemplateEngine

class WorkflowMultiplesequencealign {

    //
    // Check and validate parameters
    //
    public static void initialise(params, log) {

    }

    //
    // Get workflow summary for MultiQC
    //
    public static String paramsSummaryMultiqc(workflow, summary) {
        String summary_section = ''
        for (group in summary.keySet()) {
            def group_params = summary.get(group)  // This gets the parameters of that particular group
            if (group_params) {
                summary_section += "    <p style=\"font-size:110%\"><b>$group</b></p>\n"
                summary_section += "    <dl class=\"dl-horizontal\">\n"
                for (param in group_params.keySet()) {
                    summary_section += "        <dt>$param</dt><dd><samp>${group_params.get(param) ?: '<span style=\"color:#999999;\">N/A</a>'}</samp></dd>\n"
                }
                summary_section += "    </dl>\n"
            }
        }

        String yaml_file_text  = "id: '${workflow.manifest.name.replace('/','-')}-summary'\n"
        yaml_file_text        += "description: ' - this information is collected when the pipeline is started.'\n"
        yaml_file_text        += "section_name: '${workflow.manifest.name} Workflow Summary'\n"
        yaml_file_text        += "section_href: 'https://github.com/${workflow.manifest.name}'\n"
        yaml_file_text        += "plot_type: 'html'\n"
        yaml_file_text        += "data: |\n"
        yaml_file_text        += "${summary_section}"
        return yaml_file_text
    }

    //
    // Generate methods description for MultiQC
    //

    public static String toolCitationText(params) {

        // TODO nf-core: Optionally add in-text citation tools to this list.
        // Can use ternary operators to dynamically construct based conditions, e.g. params["run_xyz"] ? "Tool (Foo et al. 2023)" : "",
        // Uncomment function in methodsDescriptionText to render in MultiQC report
        def citation_text = [
                "Tools used in the workflow included:",
                "MultiQC (Ewels et al. 2016)",
                "."
            ].join(' ').trim()

        return citation_text
    }

    public static String toolBibliographyText(params) {

        // TODO Optionally add bibliographic entries to this list.
        // Can use ternary operators to dynamically construct based conditions, e.g. params["run_xyz"] ? "<li>Author (2023) Pub name, Journal, DOI</li>" : "",
        // Uncomment function in methodsDescriptionText to render in MultiQC report
        def reference_text = [
                "<li>Ewels, P., Magnusson, M., Lundin, S., & Käller, M. (2016). MultiQC: summarize analysis results for multiple tools and samples in a single report. Bioinformatics , 32(19), 3047–3048. doi: /10.1093/bioinformatics/btw354</li>"
            ].join(' ').trim()

        return reference_text
    }

    public static String methodsDescriptionText(run_workflow, mqc_methods_yaml, params) {
        // Convert  to a named map so can be used as with familar NXF ${workflow} variable syntax in the MultiQC YML file
        def meta = [:]
        meta.workflow = run_workflow.toMap()
        meta["manifest_map"] = run_workflow.manifest.toMap()

        // Pipeline DOI
        meta["doi_text"] = meta.manifest_map.doi ? "(doi: <a href=\'https://doi.org/${meta.manifest_map.doi}\'>${meta.manifest_map.doi}</a>)" : ""
        meta["nodoi_text"] = meta.manifest_map.doi ? "": "<li>If available, make sure to update the text to include the Zenodo DOI of version of the pipeline used. </li>"

        // Tool references
        meta["tool_citations"] = ""
        meta["tool_bibliography"] = ""

        // TODO Only uncomment below if logic in toolCitationText/toolBibliographyText has been filled!
        //meta["tool_citations"] = toolCitationText(params).replaceAll(", \\.", ".").replaceAll("\\. \\.", ".").replaceAll(", \\.", ".")
        //meta["tool_bibliography"] = toolBibliographyText(params)


        def methods_text = mqc_methods_yaml.text

        def engine =  new SimpleTemplateEngine()
        def description_html = engine.createTemplate(methods_text).make(meta)

        return description_html
    }

    public static cleanArgs(argString) {
        def cleanArgs = argString.toString().trim().replace("-", "").replace("  ", " ").replace(" ", "_").replaceAll("==", "_").replaceAll("\\s+", "")
        // if clearnArgs is empty, return ""

        if (cleanArgs == null || cleanArgs == "") {
            return ""
        }else{
            return cleanArgs
        }
    }

    public static fix_args(tool,args,tool_to_be_checked, required_flag, default_value) {
        /*
        This function checks if the required_flag is present in the args string for the tool_to_be_checked.
        If not, it adds the required_flag and the default_value to the args string.
        */
        if(tool == tool_to_be_checked){
            if( args == null || args == ""|| args == "null" || !args.contains(required_flag+" ")){
                if(args == null || args == ""|| args == "null"){
                    args = ""
                }
                args = args + " " + required_flag + " " + default_value
            }
        }
        return args
    }

    public static check_required_args(tool,args){

        // 3DCOFFEE
        args = fix_args(tool,args,"3DCOFFEE", "-method", "TMalign_pair")
        // REGRESSIVE
        args = fix_args(tool,args,"REGRESSIVE", "-reg", "")
        args = fix_args(tool,args,"REGRESSIVE", "-reg_method", "famsa_msa")
        args = fix_args(tool,args,"REGRESSIVE", "-reg_nseq", "1000")
        args = fix_args(tool,args,"REGRESSIVE", "-output", "fasta_aln")
        // TCOFFEE
        args = fix_args(tool,args,"TCOFFEE", "-output", "fasta_aln")

        return args

    }



}
