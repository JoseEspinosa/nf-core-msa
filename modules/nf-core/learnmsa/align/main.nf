process LEARNMSA_ALIGN {
    tag "$meta.id"
    label 'process_medium'
    container "oras://community.wave.seqera.io/library/mmseqs2_pigz_python_tf-keras_pruned:e72a5f6b969868d5"

    conda "${moduleDir}/environment.yml"
    container "${ workflow.containerEngine == 'singularity' && !task.ext.singularity_pull_docker_container ?
        'oras://community.wave.seqera.io/library/mmseqs2_pigz_python_tf-keras_pruned:e72a5f6b969868d5':
        'community.wave.seqera.io/library/mmseqs2_pigz_python_tf-keras_pruned:36afb1dbd73d19ec' }"

    input:
    tuple val(meta), path(fasta)

    output:
    tuple val(meta), path("*.aln")      , emit: alignment
    path "versions.yml"                 , emit: versions

    when:
    task.ext.when == null || task.ext.when

    script:
    def args   = task.ext.args ?: ''
    def prefix = task.ext.prefix ?: "${meta.id}"
    """
    learnMSA \\
        -i $fasta \\
        -o "${prefix}.aln" \\
        --plm_cache_dir tmp \\
        $args

    cat <<-END_VERSIONS > versions.yml
    "${task.process}":
        learnmsa: \$(learnMSA -h | grep 'version' | awk -F 'version ' '{print \$2}' | awk '{print \$1}' | sed 's/)//g')
    END_VERSIONS
    """

    stub:
    def args = task.ext.args ?: ''
    def prefix = task.ext.prefix ?: "${meta.id}"
    """
    touch ${prefix}.aln

    cat <<-END_VERSIONS > versions.yml
    "${task.process}":
        learnmsa: \$(if command -v learnMSA &>/dev/null; then learnMSA -h | grep 'version' | awk -F 'version ' '{print \$2}' | awk '{print \$1}' | sed 's/)//g'; else echo "STUB_TEST_HARDCODED_VERSION"; fi)
    END_VERSIONS
    """
}
