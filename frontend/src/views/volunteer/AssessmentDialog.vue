<template>
  <v-dialog v-model="dialog" persistent width="1300">
    <v-card>
      <v-card-title>
        <span class="headline"> New Assessment </span>
      </v-card-title>
      <v-card-text>
        <v-form ref="form" lazy-validation>
          <v-row>
            <v-col cols="12">
              <v-text-field
                label="*Review"
                :rules="[
                  (value) =>
                    !!value || 'Review with at least 10 characters is required',
                ]"
                required
                v-model="assessment.review"
                data-cy="reviewInput"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="blue-darken-1"
          variant="text"
          @click="$emit('close-assessment-dialog')"
        >
          Close
        </v-btn>
        <v-btn
          v-if="isReviewValid()"
          color="blue-darken-1"
          variant="text"
          @click="saveAssessment"
          data-cy="saveAssessment"
        >
          Save
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
import { Vue, Component, Model } from 'vue-property-decorator';
import Assessment from '@/models/assessment/Assessment';

@Component({})
export default class AssessmentDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;

  assessment: Assessment = new Assessment();

  async created() {}

  isReviewValid() {
    if (this.assessment.review == null) return false;
    return this.assessment.review.length >= 10;
  }

  async saveAssessment() {}
}
</script>

<style scoped lang="scss"></style>
